package com.orudesk.orudesk.admissions.domain;

public class AdmissionNumberTakenException extends RuntimeException {

    public AdmissionNumberTakenException(String admissionNumber) {
        super("This school already has a student with admission number " + admissionNumber);
    }
}
