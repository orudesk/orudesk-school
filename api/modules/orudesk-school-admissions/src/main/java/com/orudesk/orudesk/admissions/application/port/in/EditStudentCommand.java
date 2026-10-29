package com.orudesk.orudesk.admissions.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

/**
 * The student's form as it should now be, with the two versions it was read at (StudentDetails.version and
 * personVersion).
 */
public record EditStudentCommand(UUID studentId, long version, long personVersion, String firstName, String lastName,
                                 LocalDate dateOfBirth, String className, String section) {
}
