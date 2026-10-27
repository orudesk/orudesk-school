package com.orudesk.orudesk.admissions.application.port.out;

import com.orudesk.orudesk.admissions.domain.Student;

public interface SaveStudentPort {

    /** @throws com.orudesk.orudesk.admissions.domain.AdmissionNumberTakenException if the school has it already */
    void save(Student student);
}
