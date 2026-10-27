package com.orudesk.orudesk.admissions.application.service;

import com.orudesk.orudesk.identity.OrgUnitAccess;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Who may manage admissions where: an admissions officer or an admin, at that school or above it.
 * Built on identity's public OrgUnitAccess (Day 19), which already knows that a grant at the group covers
 * every school below it, and that a tenant-wide grant covers them all.
 */
@Component
class SchoolAccess {

    private static final String ADMIN = "ADMIN";

    private final OrgUnitAccess access;

    SchoolAccess(OrgUnitAccess access) {
        this.access = access;
    }

    boolean mayManage(UUID schoolId) {
        return access.currentUserHasRoleAt(AdmissionsRoles.ADMISSIONS_OFFICER, schoolId)
                || access.currentUserHasRoleAt(ADMIN, schoolId);
    }

    /** Every unit where the user may manage admissions. */
    Set<UUID> schools() {
        Set<UUID> units = new HashSet<>(access.unitsWhereCurrentUserHas(AdmissionsRoles.ADMISSIONS_OFFICER));
        units.addAll(access.unitsWhereCurrentUserHas(ADMIN));
        return units;
    }
}
