-- A student is a person from the People registry, admitted to one school: an org unit of the tenant (Day 18).
CREATE TABLE sch_student (
    id                UUID          NOT NULL,
    tenant_id         UUID          NOT NULL,
    person_id         UUID          NOT NULL,
    org_unit_id       UUID          NOT NULL,      -- the school
    admission_number  VARCHAR(30)   NOT NULL,      -- the school's own numbering, entered at admission
    class_name        VARCHAR(20)   NOT NULL,      -- "LKG", "5", "XII"
    section           VARCHAR(5)    NULL,          -- "A"; small schools have none
    admitted_on       DATE          NOT NULL,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_sch_student_tenant FOREIGN KEY (tenant_id) REFERENCES iam_tenant (id),
    CONSTRAINT fk_sch_student_person FOREIGN KEY (person_id) REFERENCES party_person (id) ON DELETE RESTRICT,
    -- The tenant is part of the key: a student can only be admitted to a school of their own tenant.
    -- iam_org_unit has UNIQUE (tenant_id, id) for exactly this (Day 18).
    CONSTRAINT fk_sch_student_school FOREIGN KEY (tenant_id, org_unit_id) REFERENCES iam_org_unit (tenant_id, id),
    -- One admission per person in the group: moving schools is a transfer, not a second admission
    CONSTRAINT uk_sch_student_person UNIQUE (tenant_id, person_id),
    -- Each school numbers its own students; two schools may both have "2026/001"
    CONSTRAINT uk_sch_student_admission_number UNIQUE (org_unit_id, admission_number)
);

-- The class lists: "this school's students, by class and section"
CREATE INDEX ix_sch_student_school_class ON sch_student (tenant_id, org_unit_id, class_name, section);

-- Student ↔ guardian: many to many. A student has one or two guardians; a guardian may have several
-- children here (siblings). The relationship and "primary contact" belong to the link, not to either person.
CREATE TABLE sch_student_guardian (
    student_id          UUID         NOT NULL,
    guardian_person_id  UUID         NOT NULL,
    relationship        VARCHAR(20)  NOT NULL,
    is_primary          BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (student_id, guardian_person_id),
    CONSTRAINT fk_sch_student_guardian_student FOREIGN KEY (student_id) REFERENCES sch_student (id) ON DELETE CASCADE,
    CONSTRAINT fk_sch_student_guardian_person FOREIGN KEY (guardian_person_id) REFERENCES party_person (id)
        ON DELETE RESTRICT,
    CONSTRAINT ck_sch_student_guardian_relationship
        CHECK (relationship IN ('MOTHER', 'FATHER', 'GRANDPARENT', 'SIBLING', 'GUARDIAN', 'OTHER'))
);

-- At most one primary contact per student: a partial unique index, unique only among the rows where it's true
CREATE UNIQUE INDEX ux_sch_student_guardian_one_primary ON sch_student_guardian (student_id) WHERE is_primary;

-- "Which students is this person the guardian of?" (siblings, a parent's view). The primary key starts
-- with student_id, so it can't answer that; this index can.
CREATE INDEX ix_sch_student_guardian_person ON sch_student_guardian (guardian_person_id);
