package com.orudesk.orudesk.admissions.application.port.out;

import com.orudesk.orudesk.admissions.application.port.in.StudentListItem;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/** The read side: students with their names, from the v_sch_student_summary view. */
public interface ReadStudentsPort {

    List<StudentListItem> listAt(Collection<UUID> schoolIds, String className, int limit);
}
