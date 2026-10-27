package com.orudesk.orudesk.admissions.application.service;

import com.orudesk.orudesk.identity.RoleDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** The role this module brings. Given per school ("admissions officer at Sunrise North"), not tenant-wide. */
@Configuration
class AdmissionsRoles {

    static final String ADMISSIONS_OFFICER = "ADMISSIONS_OFFICER";

    @Bean
    RoleDefinition admissionsOfficerRole() {
        return new RoleDefinition(ADMISSIONS_OFFICER, "Admissions officer",
                "Admits students and sees their guardians, at the schools where it's given");
    }
}
