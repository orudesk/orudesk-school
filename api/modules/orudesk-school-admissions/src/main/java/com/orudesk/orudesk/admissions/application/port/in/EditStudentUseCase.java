package com.orudesk.orudesk.admissions.application.port.in;

/**
 * Changes a student's details (Day 49): the child's in the registry, and the class and section here. Throws the
 * core's EditConflictException if either changed since it was read, and StudentNotFoundException for a student
 * of a school the user doesn't manage.
 */
public interface EditStudentUseCase {

    StudentDetails edit(EditStudentCommand command);
}
