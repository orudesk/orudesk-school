package com.orudesk.orudesk.admissions.adapter.in.web;

import com.orudesk.orudesk.admissions.domain.Relationship;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** A guardian in POST /api/v1/admissions: an existing person's id, or a new person's details. */
record GuardianRequest(
        UUID existingPersonId,
        @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        @Size(max = 30) String phone,
        @Email @Size(max = 320) String email,
        @NotNull Relationship relationship,
        Boolean primary) {

    /** A rule across fields: Bean Validation checks any boolean isXxx() method annotated like this. */
    @AssertTrue(message = "give existingPersonId, or firstName and lastName")
    boolean isIdentified() {
        return existingPersonId != null
                || (firstName != null && !firstName.isBlank() && lastName != null && !lastName.isBlank());
    }
}
