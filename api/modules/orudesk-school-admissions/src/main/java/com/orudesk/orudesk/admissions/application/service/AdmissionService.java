package com.orudesk.orudesk.admissions.application.service;

import com.orudesk.orudesk.admissions.application.port.in.AdmitStudentCommand;
import com.orudesk.orudesk.admissions.application.port.in.AdmitStudentUseCase;
import com.orudesk.orudesk.admissions.application.port.in.EditStudentCommand;
import com.orudesk.orudesk.admissions.application.port.in.EditStudentUseCase;
import com.orudesk.orudesk.admissions.application.port.in.GetStudentQuery;
import com.orudesk.orudesk.admissions.application.port.in.GuardianDetails;
import com.orudesk.orudesk.admissions.application.port.in.GuardianInput;
import com.orudesk.orudesk.admissions.application.port.in.ListStudentsQuery;
import com.orudesk.orudesk.admissions.application.port.in.StudentDetails;
import com.orudesk.orudesk.admissions.application.port.in.StudentEdited;
import com.orudesk.orudesk.admissions.application.port.in.StudentAdmitted;
import com.orudesk.orudesk.admissions.application.port.in.StudentListItem;
import com.orudesk.orudesk.admissions.application.port.out.LoadStudentPort;
import com.orudesk.orudesk.admissions.application.port.out.ReadStudentsPort;
import com.orudesk.orudesk.admissions.application.port.out.SaveStudentPort;
import com.orudesk.orudesk.admissions.domain.GuardianLink;
import com.orudesk.orudesk.admissions.domain.NotAllowedAtSchoolException;
import com.orudesk.orudesk.admissions.domain.Student;
import com.orudesk.orudesk.admissions.domain.StudentId;
import com.orudesk.orudesk.admissions.domain.StudentNotFoundException;
import com.orudesk.orudesk.party.application.port.in.EditPersonCommand;
import com.orudesk.orudesk.party.application.port.in.EditPersonUseCase;
import com.orudesk.orudesk.party.application.port.in.GetPersonQuery;
import com.orudesk.orudesk.party.application.port.in.PersonDetails;
import com.orudesk.orudesk.party.application.port.in.RegisterPersonCommand;
import com.orudesk.orudesk.party.application.port.in.RegisterPersonUseCase;
import com.orudesk.orudesk.shared.EditConflictException;
import com.orudesk.orudesk.shared.FieldChange;
import com.orudesk.orudesk.shared.FieldChanges;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
class AdmissionService implements AdmitStudentUseCase, EditStudentUseCase, GetStudentQuery, ListStudentsQuery {

    private final RegisterPersonUseCase registerPerson;
    private final EditPersonUseCase editPerson;
    private final GetPersonQuery getPerson;
    private final SaveStudentPort saveStudentPort;
    private final LoadStudentPort loadStudentPort;
    private final ReadStudentsPort readStudentsPort;
    private final SchoolAccess schoolAccess;
    private final Clock clock;
    private final ApplicationEventPublisher events;

    AdmissionService(RegisterPersonUseCase registerPerson, EditPersonUseCase editPerson, GetPersonQuery getPerson,
                     SaveStudentPort saveStudentPort, LoadStudentPort loadStudentPort,
                     ReadStudentsPort readStudentsPort, SchoolAccess schoolAccess, Clock clock,
                     ApplicationEventPublisher events) {
        this.registerPerson = registerPerson;
        this.editPerson = editPerson;
        this.getPerson = getPerson;
        this.saveStudentPort = saveStudentPort;
        this.loadStudentPort = loadStudentPort;
        this.readStudentsPort = readStudentsPort;
        this.schoolAccess = schoolAccess;
        this.clock = clock;
        this.events = events;
    }

    /**
     * Up to five inserts in one transaction: the child, new guardians (people), the student, the links. If
     * anything fails (a rule, a taken admission number), none of it stays: no half-admitted child, and no
     * guardians in the registry for an admission that never happened.
     */
    @Override
    @Transactional
    public UUID admit(UUID schoolId, AdmitStudentCommand command) {
        if (!schoolAccess.mayManage(schoolId)) {
            throw new NotAllowedAtSchoolException(schoolId);
        }

        List<GuardianLink> guardians = command.guardians().stream()
                .map(guardian -> new GuardianLink(guardianPerson(guardian), guardian.relationship(), guardian.primary()))
                .toList();
        UUID child = registerPerson.register(new RegisterPersonCommand(
                command.firstName(), command.lastName(), command.dateOfBirth(), null, null));

        Student student = Student.admit(child, schoolId, command.admissionNumber(), command.className(),
                command.section(), guardians, LocalDate.now(clock));
        saveStudentPort.save(student);
        events.publishEvent(new StudentAdmitted(student.id().value(), command.firstName() + " " + command.lastName(),
                command.admissionNumber(), command.className()));
        return student.id().value();
    }

    /** An existing person (checked: in this tenant, or a 404), or a new one in the registry. */
    private UUID guardianPerson(GuardianInput guardian) {
        if (guardian.existingPersonId() != null) {
            return getPerson.getPerson(guardian.existingPersonId()).id();
        }
        return registerPerson.register(new RegisterPersonCommand(
                guardian.firstName(), guardian.lastName(), null, guardian.phone(), guardian.email()));
    }

    /**
     * The child's details in the registry and the class here, in one transaction (as for a patient). The registry
     * edit is a replace, so the child's phone and email, which this form doesn't show, are sent back as they are.
     */
    @Override
    @Transactional
    public StudentDetails edit(EditStudentCommand command) {
        StudentId id = new StudentId(command.studentId());
        Student current = managed(id);
        if (current.version() != command.version()) {
            throw new EditConflictException("student", id.value(), command.version(), current.version());
        }
        Student moved = current.moveTo(command.className(), command.section());

        PersonDetails child = getPerson.getPerson(current.personId());
        editPerson.edit(new EditPersonCommand(child.id(), command.personVersion(), command.firstName(),
                command.lastName(), command.dateOfBirth(), child.phone(), child.email()));

        List<FieldChange> changes = new FieldChanges()
                .compare("className", current.className(), moved.className())
                .compare("section", current.section(), moved.section())
                .list();
        if (!changes.isEmpty()) {
            saveStudentPort.update(moved);
            events.publishEvent(new StudentEdited(id.value(), current.admissionNumber(), changes));
        }
        return getStudent(id.value());
    }

    @Override
    @Transactional(readOnly = true)
    public StudentDetails getStudent(UUID studentId) {
        StudentId id = new StudentId(studentId);
        Student student = managed(id);

        // One registry lookup per person: fine for a child and two or three guardians. A list would use
        // the view instead (see list()).
        PersonDetails child = getPerson.getPerson(student.personId());
        List<GuardianDetails> guardians = student.guardians().stream()
                .map(link -> {
                    PersonDetails person = getPerson.getPerson(link.personId());
                    return new GuardianDetails(person.id(), person.firstName(), person.lastName(), person.phone(),
                            link.relationship(), link.primary());
                })
                .toList();
        return new StudentDetails(student.id().value(), student.schoolId(), child.id(), child.firstName(),
                child.lastName(), child.dateOfBirth(), student.admissionNumber(), student.className(),
                student.section(), student.admittedOn(), guardians, student.version(), child.version());
    }

    /** A student at a school the user manages; anyone else's is "not found", not "forbidden". */
    private Student managed(StudentId id) {
        return loadStudentPort.findById(id)
                .filter(found -> schoolAccess.mayManage(found.schoolId()))
                .orElseThrow(() -> new StudentNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentListItem> list(String className) {
        Set<UUID> schools = schoolAccess.schools();
        if (schools.isEmpty()) {
            return List.of();   // and no query: "IN ()" isn't valid SQL anyway
        }
        return readStudentsPort.listAt(schools, className, MAX_STUDENTS);
    }
}
