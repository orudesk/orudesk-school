package com.orudesk.orudesk.admissions.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StudentTest {

    private static final UUID CHILD = UUID.randomUUID();
    private static final UUID SCHOOL = UUID.randomUUID();
    private static final UUID MOTHER = UUID.randomUUID();
    private static final UUID FATHER = UUID.randomUUID();
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 10);

    private static Student admit(List<GuardianLink> guardians) {
        return Student.admit(CHILD, SCHOOL, "2026/001", "5", "A", guardians, TODAY);
    }

    @Test
    void admitsWithOnePrimaryGuardian() {
        Student student = admit(List.of(new GuardianLink(MOTHER, Relationship.MOTHER, true),
                new GuardianLink(FATHER, Relationship.FATHER, false)));

        assertThat(student.guardians()).hasSize(2);
        assertThat(student.admittedOn()).isEqualTo(TODAY);
    }

    @Test
    void needsAGuardian() {
        assertThatThrownBy(() -> admit(List.of())).hasMessage("a student needs 1 to 3 guardians");
    }

    @Test
    void needsExactlyOnePrimaryContact() {
        assertThatThrownBy(() -> admit(List.of(new GuardianLink(MOTHER, Relationship.MOTHER, true),
                new GuardianLink(FATHER, Relationship.FATHER, true))))
                .hasMessage("exactly one guardian must be the primary contact");
        assertThatThrownBy(() -> admit(List.of(new GuardianLink(MOTHER, Relationship.MOTHER, false))))
                .hasMessage("exactly one guardian must be the primary contact");
    }

    @Test
    void refusesTheSameGuardianTwice() {
        assertThatThrownBy(() -> admit(List.of(new GuardianLink(MOTHER, Relationship.MOTHER, true),
                new GuardianLink(MOTHER, Relationship.GUARDIAN, false))))
                .hasMessage("the same guardian is listed twice");
    }

    @Test
    void aStudentIsNotTheirOwnGuardian() {
        assertThatThrownBy(() -> admit(List.of(new GuardianLink(CHILD, Relationship.OTHER, true))))
                .hasMessage("a student can't be their own guardian");
    }

    @Test
    void anEmptySectionMeansNone() {
        Student student = Student.admit(CHILD, SCHOOL, " 2026/001 ", "LKG", " ",
                List.of(new GuardianLink(MOTHER, Relationship.MOTHER, true)), TODAY);

        assertThat(student.section()).isNull();
        assertThat(student.admissionNumber()).isEqualTo("2026/001");
    }
}
