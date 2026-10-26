package com.orudesk.orudesk.admissions.application.port.in;

import java.util.UUID;

public record StudentListItem(UUID id, UUID schoolId, String firstName, String lastName, String admissionNumber,
                              String className, String section) {
}
