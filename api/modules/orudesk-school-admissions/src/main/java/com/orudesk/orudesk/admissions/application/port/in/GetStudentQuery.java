package com.orudesk.orudesk.admissions.application.port.in;

import java.util.UUID;

public interface GetStudentQuery {

    /** A student at a school where the user may see admissions; any other student is "not found". */
    StudentDetails getStudent(UUID studentId);
}
