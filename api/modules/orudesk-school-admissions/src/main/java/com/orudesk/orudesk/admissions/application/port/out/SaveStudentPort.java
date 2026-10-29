package com.orudesk.orudesk.admissions.application.port.out;

import com.orudesk.orudesk.admissions.domain.Student;

public interface SaveStudentPort {

    /** @throws com.orudesk.orudesk.admissions.domain.AdmissionNumberTakenException if the school has it already */
    void save(Student student);

    /** Writes the new class and section if the stored row is still at student.version(); returns the new version. */
    long update(Student student);
}
