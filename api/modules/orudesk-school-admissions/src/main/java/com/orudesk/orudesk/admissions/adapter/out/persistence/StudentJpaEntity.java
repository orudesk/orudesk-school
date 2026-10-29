package com.orudesk.orudesk.admissions.adapter.out.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.TenantId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** One row of sch_student, with its guardian links from sch_student_guardian. */
@Entity
@Table(name = "sch_student")
class StudentJpaEntity {

    @Id
    private UUID id;

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "person_id", nullable = false, updatable = false)
    private UUID personId;

    @Column(name = "org_unit_id", nullable = false)
    private UUID schoolId;

    @Column(name = "admission_number", nullable = false, length = 30)
    private String admissionNumber;

    @Column(name = "class_name", nullable = false, length = 20)
    private String className;

    @Column(length = 5)
    private String section;

    @Column(name = "admitted_on", nullable = false)
    private LocalDate admittedOn;

    @Version   // optimistic locking (Day 49), as in the core's PersonJpaEntity; null until first saved
    private Long version;

    // The links are values that belong to the student, not entities of their own: the same choice as the
    // role grants on Day 19. EAGER: a student is never used without them, and there are at most three.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "sch_student_guardian", joinColumns = @JoinColumn(name = "student_id"))
    private List<GuardianLinkEmbeddable> guardians = new ArrayList<>();

    protected StudentJpaEntity() {
        // JPA needs a no-argument constructor
    }

    StudentJpaEntity(UUID id, UUID personId, UUID schoolId, String admissionNumber, String className, String section,
                     LocalDate admittedOn, List<GuardianLinkEmbeddable> guardians) {
        this.id = id;
        this.personId = personId;
        this.schoolId = schoolId;
        this.admissionNumber = admissionNumber;
        this.className = className;
        this.section = section;
        this.admittedOn = admittedOn;
        this.guardians = new ArrayList<>(guardians);
    }

    void moveTo(String className, String section) {
        this.className = className;
        this.section = section;
    }

    UUID getId() { return id; }
    UUID getPersonId() { return personId; }
    UUID getSchoolId() { return schoolId; }
    String getAdmissionNumber() { return admissionNumber; }
    String getClassName() { return className; }
    String getSection() { return section; }
    LocalDate getAdmittedOn() { return admittedOn; }
    List<GuardianLinkEmbeddable> getGuardians() { return guardians; }
    Long getVersion() { return version; }
}
