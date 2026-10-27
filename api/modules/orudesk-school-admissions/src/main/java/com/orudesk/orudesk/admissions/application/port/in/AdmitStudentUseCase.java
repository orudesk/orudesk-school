package com.orudesk.orudesk.admissions.application.port.in;

import java.util.UUID;

public interface AdmitStudentUseCase {

    /**
     * Registers the child (and any new guardians) in the People registry and admits them: all, or nothing.
     *
     * @throws com.orudesk.orudesk.admissions.domain.NotAllowedAtSchoolException without admissions rights there
     */
    UUID admit(UUID schoolId, AdmitStudentCommand command);
}
