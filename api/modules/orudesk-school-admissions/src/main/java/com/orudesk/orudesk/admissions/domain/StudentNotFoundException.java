package com.orudesk.orudesk.admissions.domain;

public class StudentNotFoundException extends RuntimeException {

    public StudentNotFoundException(StudentId id) {
        super("No student with id " + id.value());
    }
}
