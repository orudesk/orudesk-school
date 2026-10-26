package com.orudesk.orudesk.admissions.application.port.out;

import com.orudesk.orudesk.admissions.domain.Student;
import com.orudesk.orudesk.admissions.domain.StudentId;

import java.util.Optional;

public interface LoadStudentPort {

    Optional<Student> findById(StudentId id);
}
