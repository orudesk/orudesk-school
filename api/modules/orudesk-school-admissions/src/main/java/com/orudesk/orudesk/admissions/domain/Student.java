package com.orudesk.orudesk.admissions.domain;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * A student admitted to one school. Name and date of birth stay in the People registry (personId); the
 * school is an org unit of the tenant (schoolId).
 */
public final class Student {

    public static final int MAX_GUARDIANS = 3;

    private final StudentId id;
    private final UUID personId;
    private final UUID schoolId;
    private final String admissionNumber;
    private final String className;
    private final String section;
    private final LocalDate admittedOn;
    private final List<GuardianLink> guardians;
    private final long version;

    private Student(StudentId id, UUID personId, UUID schoolId, String admissionNumber, String className,
                    String section, LocalDate admittedOn, List<GuardianLink> guardians, long version) {
        this.id = Objects.requireNonNull(id);
        this.personId = Objects.requireNonNull(personId, "a student is a person");
        this.schoolId = Objects.requireNonNull(schoolId, "a student is admitted to a school");
        this.admissionNumber = required(admissionNumber, "admission number", 30);
        this.className = required(className, "class", 20);
        this.section = section == null || section.isBlank() ? null : required(section, "section", 5);
        this.admittedOn = Objects.requireNonNull(admittedOn);
        this.guardians = List.copyOf(guardians);
        this.version = version;
    }

    /**
     * The rules of an admission: one to three guardians, nobody twice, exactly one primary contact, and the
     * student isn't their own guardian.
     */
    public static Student admit(UUID personId, UUID schoolId, String admissionNumber, String className,
                                String section, List<GuardianLink> guardians, LocalDate today) {
        if (guardians.isEmpty() || guardians.size() > MAX_GUARDIANS) {
            throw new IllegalArgumentException("a student needs 1 to " + MAX_GUARDIANS + " guardians");
        }
        if (guardians.stream().filter(GuardianLink::primary).count() != 1) {
            throw new IllegalArgumentException("exactly one guardian must be the primary contact");
        }
        var people = new HashSet<UUID>();
        for (GuardianLink guardian : guardians) {
            if (guardian.personId().equals(personId)) {
                throw new IllegalArgumentException("a student can't be their own guardian");
            }
            if (!people.add(guardian.personId())) {
                throw new IllegalArgumentException("the same guardian is listed twice");
            }
        }
        return new Student(StudentId.newId(), personId, schoolId, admissionNumber, className, section, today,
                guardians, 0);
    }

    /** For the persistence adapter: rebuilds a student that already exists, without re-checking the rules. */
    public static Student restore(StudentId id, UUID personId, UUID schoolId, String admissionNumber,
                                  String className, String section, LocalDate admittedOn,
                                  List<GuardianLink> guardians, long version) {
        return new Student(id, personId, schoolId, admissionNumber, className, section, admittedOn, guardians,
                version);
    }

    /**
     * The student in another class or section (Day 49): a correction, or a move between sections. The same
     * length rules as at admission. The admission number, school and guardians don't change here: a number is the
     * school's record of the admission, and a move to another school is a transfer.
     */
    public Student moveTo(String newClassName, String newSection) {
        return new Student(id, personId, schoolId, admissionNumber, newClassName, newSection, admittedOn, guardians,
                version);
    }

    private static String required(String value, String what, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(what + " is required");
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(what + " is at most " + maxLength + " characters");
        }
        return trimmed;
    }

    public StudentId id() { return id; }
    public UUID personId() { return personId; }
    public UUID schoolId() { return schoolId; }
    public String admissionNumber() { return admissionNumber; }
    public String className() { return className; }
    public String section() { return section; }
    public LocalDate admittedOn() { return admittedOn; }
    public List<GuardianLink> guardians() { return guardians; }
    public long version() { return version; }
}
