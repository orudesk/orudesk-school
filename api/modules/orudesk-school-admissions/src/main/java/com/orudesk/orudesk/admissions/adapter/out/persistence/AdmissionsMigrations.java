package com.orudesk.orudesk.admissions.adapter.out.persistence;

import com.orudesk.orudesk.shared.ModuleMigrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** sch_ tables point at party_person and iam_org_unit. No @Order: a vertical's modules run after the core's. */
@Configuration
class AdmissionsMigrations {

    @Bean
    ModuleMigrations admissionsModuleMigrations() {
        return new ModuleMigrations("admissions", "classpath:db/migration/admissions");
    }
}
