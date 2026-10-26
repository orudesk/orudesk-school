package com.orudesk.orudesk.admissions.application.port.in;

import com.orudesk.orudesk.admissions.domain.Relationship;

import java.util.UUID;

public record GuardianDetails(UUID personId, String firstName, String lastName, String phone,
                              Relationship relationship, boolean primary) {
}
