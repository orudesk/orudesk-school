package com.orudesk.orudesk.admissions.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * One guardian of a student: a person from the registry, how they're related, and whether the school
 * calls them first. It belongs to the link, not to the person: the same mother is the primary contact for
 * one child and not for another.
 */
public record GuardianLink(UUID personId, Relationship relationship, boolean primary) {

    public GuardianLink {
        Objects.requireNonNull(personId, "a guardian is a person");
        Objects.requireNonNull(relationship, "relationship must not be null");
    }
}
