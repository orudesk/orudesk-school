package com.orudesk.orudesk.admissions.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.TenantId;

import java.util.UUID;

/** One row of the view v_sch_student_summary: read only, tenant filtered (as Day 23's donor summary). */
@Entity
@Immutable
@Table(name = "v_sch_student_summary")
class StudentSummaryJpaEntity {

    @Id
    private UUID id;

    @TenantId
    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "org_unit_id")
    private UUID schoolId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "admission_number")
    private String admissionNumber;

    @Column(name = "class_name")
    private String className;

    private String section;

    protected StudentSummaryJpaEntity() {
        // JPA needs a no-argument constructor
    }

    UUID getId() { return id; }
    UUID getSchoolId() { return schoolId; }
    String getFirstName() { return firstName; }
    String getLastName() { return lastName; }
    String getAdmissionNumber() { return admissionNumber; }
    String getClassName() { return className; }
    String getSection() { return section; }
}
