package com.orudesk.orudesk.admissions.application.port.in;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

public record StudentListItem(UUID id, UUID schoolId, String firstName, String lastName, String admissionNumber,
                              String className, @Nullable String section) {
}
