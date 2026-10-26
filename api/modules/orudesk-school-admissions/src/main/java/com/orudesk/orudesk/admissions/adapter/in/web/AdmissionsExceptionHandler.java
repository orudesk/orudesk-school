package com.orudesk.orudesk.admissions.adapter.in.web;

import com.orudesk.orudesk.admissions.domain.AdmissionNumberTakenException;
import com.orudesk.orudesk.admissions.domain.NotAllowedAtSchoolException;
import com.orudesk.orudesk.admissions.domain.StudentNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class AdmissionsExceptionHandler {

    @ExceptionHandler(StudentNotFoundException.class)
    ProblemDetail studentNotFound(StudentNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, "Student not found", e);
    }

    @ExceptionHandler(AdmissionNumberTakenException.class)
    ProblemDetail admissionNumberTaken(AdmissionNumberTakenException e) {
        return problem(HttpStatus.CONFLICT, "Admission number taken", e);
    }

    @ExceptionHandler(NotAllowedAtSchoolException.class)
    ProblemDetail notAllowed(NotAllowedAtSchoolException e) {
        return problem(HttpStatus.FORBIDDEN, "Not allowed at this school", e);
    }

    private static ProblemDetail problem(HttpStatus status, String title, RuntimeException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, e.getMessage());
        problem.setTitle(title);
        return problem;
    }
}
