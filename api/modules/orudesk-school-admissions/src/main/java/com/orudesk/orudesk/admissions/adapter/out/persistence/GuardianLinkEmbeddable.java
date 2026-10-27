package com.orudesk.orudesk.admissions.adapter.out.persistence;

import com.orudesk.orudesk.admissions.domain.Relationship;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.UUID;

/** One row of sch_student_guardian, without student_id (the owning student's id fills it). */
@Embeddable
class GuardianLinkEmbeddable {

    @Column(name = "guardian_person_id", nullable = false)
    private UUID personId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Relationship relationship;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    protected GuardianLinkEmbeddable() {
        // JPA needs a no-argument constructor
    }

    GuardianLinkEmbeddable(UUID personId, Relationship relationship, boolean primary) {
        this.personId = personId;
        this.relationship = relationship;
        this.primary = primary;
    }

    UUID getPersonId() { return personId; }
    Relationship getRelationship() { return relationship; }
    boolean isPrimary() { return primary; }
}
