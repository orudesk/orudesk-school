-- A student with their name from the People registry: the class lists read this (the same pattern as the
-- NGO's v_ngo_donor_summary, Day 23). The only SQL in this edition that names party's columns.
CREATE OR REPLACE VIEW v_sch_student_summary AS
SELECT s.id,
       s.tenant_id,
       s.org_unit_id,
       s.person_id,
       p.first_name,
       p.last_name,
       p.date_of_birth,
       s.admission_number,
       s.class_name,
       s.section,
       s.admitted_on
FROM sch_student s
         JOIN party_person p ON p.id = s.person_id AND p.tenant_id = s.tenant_id;
