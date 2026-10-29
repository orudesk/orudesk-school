package com.orudesk.orudesk.admissions.application.port.in;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * @param version       the student record's version, for an edit (Day 49)
 * @param personVersion the child's version in the People registry, where the name and date of birth live
 */
public record StudentDetails(UUID id, UUID schoolId, UUID personId, String firstName, String lastName,
                             LocalDate dateOfBirth, String admissionNumber, String className, @Nullable String section,
                             LocalDate admittedOn, List<GuardianDetails> guardians, long version, long personVersion) {
}
