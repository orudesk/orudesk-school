package com.orudesk.orudesk.admissions.application.port.in;

import com.orudesk.orudesk.shared.AuditableEvent;
import com.orudesk.orudesk.shared.FieldChange;

import java.util.List;
import java.util.UUID;

/** A student's class or section changed (Day 49). Changes to the child's name are the core's party.person.edited. */
public record StudentEdited(UUID studentId, String admissionNumber, List<FieldChange> changes) implements AuditableEvent {

    public StudentEdited {
        changes = List.copyOf(changes);
    }

    @Override public String action() { return "admissions.student.edited"; }
    @Override public String subjectType() { return "student"; }
    @Override public UUID subjectId() { return studentId; }

    @Override
    public String summary() {
        return "Changed " + String.join(", ", changes.stream().map(FieldChange::field).toList()) + " of student no. "
                + admissionNumber;
    }
}
