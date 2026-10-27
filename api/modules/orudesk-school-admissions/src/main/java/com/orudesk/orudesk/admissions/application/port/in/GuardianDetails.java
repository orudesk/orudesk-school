package com.orudesk.orudesk.admissions.application.port.in;

import com.orudesk.orudesk.admissions.domain.Relationship;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public record GuardianDetails(UUID personId, String firstName, String lastName, @Nullable String phone,
                              Relationship relationship, boolean primary) {
}
