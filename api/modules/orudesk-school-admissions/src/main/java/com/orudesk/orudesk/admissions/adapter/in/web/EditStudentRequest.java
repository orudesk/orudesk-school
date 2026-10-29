package com.orudesk.orudesk.admissions.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

/** The JSON body of PUT /api/v1/students/{id}. */
record EditStudentRequest(
        @NotNull @PositiveOrZero Long version,
        @NotNull @PositiveOrZero Long personVersion,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotNull @PastOrPresent LocalDate dateOfBirth,
        @NotBlank @Size(max = 20) String className,
        @Size(max = 5) @Nullable String section) {
}
