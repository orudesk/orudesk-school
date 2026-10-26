package com.orudesk.orudesk.admissions.application.port.in;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record StudentDetails(UUID id, UUID schoolId, UUID personId, String firstName, String lastName,
                             LocalDate dateOfBirth, String admissionNumber, String className, @Nullable String section,
                             LocalDate admittedOn, List<GuardianDetails> guardians) {
}
