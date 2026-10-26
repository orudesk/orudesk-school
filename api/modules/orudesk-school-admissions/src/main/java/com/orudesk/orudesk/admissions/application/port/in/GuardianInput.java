package com.orudesk.orudesk.admissions.application.port.in;

import com.orudesk.orudesk.admissions.domain.Relationship;

import java.util.UUID;

/**
 * A guardian at admission: either someone already in the registry (existingPersonId, e.g. the parent of a
 * sibling admitted last year), or a new person (the names, phone and email).
 */
public record GuardianInput(UUID existingPersonId, String firstName, String lastName, String phone, String email,
                            Relationship relationship, boolean primary) {
}
