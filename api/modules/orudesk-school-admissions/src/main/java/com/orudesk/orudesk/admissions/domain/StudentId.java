package com.orudesk.orudesk.admissions.domain;

import java.util.Objects;
import java.util.UUID;

public record StudentId(UUID value) {

    public StudentId {
        Objects.requireNonNull(value, "student id must not be null");
    }

    public static StudentId newId() {
        return new StudentId(UUID.randomUUID());
    }
}
