package com.orudesk.orudesk.admissions.adapter.in.web;

import com.orudesk.orudesk.admissions.application.port.in.AdmitStudentCommand;
import com.orudesk.orudesk.admissions.application.port.in.AdmitStudentUseCase;
import com.orudesk.orudesk.admissions.application.port.in.EditStudentCommand;
import com.orudesk.orudesk.admissions.application.port.in.EditStudentUseCase;
import com.orudesk.orudesk.admissions.application.port.in.GetStudentQuery;
import com.orudesk.orudesk.admissions.application.port.in.GuardianInput;
import com.orudesk.orudesk.admissions.application.port.in.ListStudentsQuery;
import com.orudesk.orudesk.admissions.application.port.in.StudentDetails;
import com.orudesk.orudesk.admissions.application.port.in.StudentListItem;
import com.orudesk.orudesk.identity.CurrentOrgUnit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * No @PreAuthorize: who may admit depends on the school, which only the service knows how to check
 * (SchoolAccess). Every endpoint still needs a logged-in user (the core's SecurityConfig).
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Admissions")
class AdmissionController {

    private final AdmitStudentUseCase admitStudent;
    private final EditStudentUseCase editStudent;
    private final GetStudentQuery getStudent;
    private final ListStudentsQuery listStudents;
    private final CurrentOrgUnit currentOrgUnit;

    AdmissionController(AdmitStudentUseCase admitStudent, EditStudentUseCase editStudent, GetStudentQuery getStudent,
                        ListStudentsQuery listStudents, CurrentOrgUnit currentOrgUnit) {
        this.admitStudent = admitStudent;
        this.editStudent = editStudent;
        this.getStudent = getStudent;
        this.listStudents = listStudents;
        this.currentOrgUnit = currentOrgUnit;
    }

    @PostMapping("/admissions")
    @ApiResponse(responseCode = "201", description = "Admitted", useReturnTypeSchema = true)
    @Operation(operationId = "admitStudent", summary = "Admit a student to the school in X-Org-Unit, with their guardians",
            parameters = @Parameter(name = CurrentOrgUnit.HEADER, in = ParameterIn.HEADER, required = true,
                    description = "The school", schema = @Schema(type = "string", format = "uuid")))
    ResponseEntity<AdmitStudentResponse> admit(@Valid @RequestBody AdmitStudentRequest request) {
        UUID school = currentOrgUnit.require();
        UUID id = admitStudent.admit(school, new AdmitStudentCommand(request.firstName(), request.lastName(),
                request.dateOfBirth(), request.admissionNumber(), request.className(), request.section(),
                request.guardians().stream()
                        .map(g -> new GuardianInput(g.existingPersonId(), g.firstName(), g.lastName(), g.phone(),
                                g.email(), g.relationship(), Boolean.TRUE.equals(g.primary())))
                        .toList()));
        return ResponseEntity.created(URI.create("/api/v1/students/" + id)).body(new AdmitStudentResponse(id));
    }

    @GetMapping("/students")
    @Operation(operationId = "listStudents", summary = "Students of every school where you manage admissions")
    List<StudentListItem> list(@RequestParam(required = false) String className) {
        return listStudents.list(className);
    }

    @GetMapping("/students/{studentId}")
    @Operation(operationId = "getStudent", summary = "One student, with their guardians")
    StudentDetails get(@PathVariable UUID studentId) {
        return getStudent.getStudent(studentId);
    }

    @PutMapping("/students/{studentId}")
    @Operation(operationId = "editStudent", summary = "Change a student's name, date of birth, class or section",
            description = "Send both versions you read. 409 if the student or the child's details were changed since.")
    StudentDetails edit(@PathVariable UUID studentId, @Valid @RequestBody EditStudentRequest request) {
        return editStudent.edit(new EditStudentCommand(studentId, request.version(), request.personVersion(),
                request.firstName(), request.lastName(), request.dateOfBirth(), request.className(), request.section()));
    }
}
