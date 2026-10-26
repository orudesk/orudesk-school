package com.orudesk.orudesk.admissions.domain;

import java.util.UUID;

/** The user has no role here that allows admissions at this school. Becomes a 403. */
public class NotAllowedAtSchoolException extends RuntimeException {

    public NotAllowedAtSchoolException(UUID schoolId) {
        super("You may not manage admissions at school " + schoolId);
    }
}
