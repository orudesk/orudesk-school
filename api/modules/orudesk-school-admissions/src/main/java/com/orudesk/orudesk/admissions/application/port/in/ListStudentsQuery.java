package com.orudesk.orudesk.admissions.application.port.in;

import java.util.List;

public interface ListStudentsQuery {

    int MAX_STUDENTS = 500;

    /**
     * The students of every school where the user may see admissions, by school, class, section and name.
     *
     * @param className only this class; null for all
     */
    List<StudentListItem> list(String className);
}
