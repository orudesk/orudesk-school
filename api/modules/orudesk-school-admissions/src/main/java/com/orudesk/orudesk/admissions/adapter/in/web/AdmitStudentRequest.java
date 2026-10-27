package com.orudesk.orudesk.admissions.adapter.in.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;

/** The JSON body of POST /api/v1/admissions. The school comes from the X-Org-Unit header. */
record AdmitStudentRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotNull LocalDate dateOfBirth,
        @NotBlank @Size(max = 30) String admissionNumber,
        @NotBlank @Size(max = 20) String className,
        @Size(max = 5) @Nullable String section,
        @NotEmpty @Size(max = 3) List<@Valid GuardianRequest> guardians) {   // @Valid: check each guardian too
}
