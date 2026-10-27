package com.orudesk.orudesk.admissions.application.port.in;

import java.time.LocalDate;
import java.util.List;

/** A new student at a school: the child's details for the registry, the school's details, the guardians. */
public record AdmitStudentCommand(String firstName, String lastName, LocalDate dateOfBirth, String admissionNumber,
                                  String className, String section, List<GuardianInput> guardians) {
}
