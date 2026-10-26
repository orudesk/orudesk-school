package com.orudesk.orudesk.admissions.adapter.out.persistence;

import com.orudesk.orudesk.admissions.application.port.out.LoadStudentPort;
import com.orudesk.orudesk.admissions.application.port.out.SaveStudentPort;
import com.orudesk.orudesk.admissions.domain.AdmissionNumberTakenException;
import com.orudesk.orudesk.admissions.domain.GuardianLink;
import com.orudesk.orudesk.admissions.domain.Student;
import com.orudesk.orudesk.admissions.domain.StudentId;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class StudentPersistenceAdapter implements SaveStudentPort, LoadStudentPort {

    private final StudentJpaRepository students;

    StudentPersistenceAdapter(StudentJpaRepository students) {
        this.students = students;
    }

    @Override
    public void save(Student student) {
        try {
            students.saveAndFlush(new StudentJpaEntity(student.id().value(), student.personId(), student.schoolId(),
                    student.admissionNumber(), student.className(), student.section(), student.admittedOn(),
                    student.guardians().stream()
                            .map(g -> new GuardianLinkEmbeddable(g.personId(), g.relationship(), g.primary()))
                            .toList()));
        } catch (DataIntegrityViolationException e) {
            if (String.valueOf(e.getMessage()).contains("uk_sch_student_admission_number")) {
                throw new AdmissionNumberTakenException(student.admissionNumber());
            }
            throw e;
        }
    }

    @Override
    public Optional<Student> findById(StudentId id) {
        return students.findById(id.value()).map(row -> Student.restore(new StudentId(row.getId()),
                row.getPersonId(), row.getSchoolId(), row.getAdmissionNumber(), row.getClassName(), row.getSection(),
                row.getAdmittedOn(), row.getGuardians().stream()
                        .map(g -> new GuardianLink(g.getPersonId(), g.getRelationship(), g.isPrimary()))
                        .toList()));
    }
}
