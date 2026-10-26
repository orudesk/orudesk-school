package com.orudesk.orudesk.admissions.application.port.in;

import com.orudesk.orudesk.shared.AuditableEvent;

import java.util.UUID;

/** A child was admitted: recorded in the core's audit log (Day 42), with who did it. */
public record StudentAdmitted(UUID studentId, String fullName, String admissionNumber, String className)
        implements AuditableEvent {

    @Override public String action() { return "admissions.student.admitted"; }
    @Override public String subjectType() { return "student"; }
    @Override public UUID subjectId() { return studentId; }
    @Override public String summary() {
        return "Admitted " + fullName + " to class " + className + " (no. " + admissionNumber + ")";
    }
}
