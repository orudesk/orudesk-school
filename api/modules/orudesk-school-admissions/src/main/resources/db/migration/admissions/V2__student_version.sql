-- Day 49: a student's class and section can be changed (a correction, or moving to another section), with
-- optimistic locking like the core's people (party V3).
ALTER TABLE sch_student ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
