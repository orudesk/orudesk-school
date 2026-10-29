package com.orudesk.orudesk;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Admissions through the whole school app. The demo tenant gets two schools under its root unit; one user
 * is admissions officer at North only. Everything is set up through the core's public HTTP API.
 */
@SpringBootTest(properties = {
        "orudesk.identity.first-admin.email=test-admin@orudesk.org",
        "orudesk.identity.first-admin.password=admin-password"
})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AdmissionsIntegrationTest {

    private static final String ROOT = "00000000-0000-0000-0000-000000000201";   // the demo tenant's root unit

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    private String admin;
    private String officer;
    private String north;
    private String south;

    @BeforeEach
    void twoSchoolsAndAnOfficerAtNorth() throws Exception {
        admin = login("test-admin@orudesk.org", "admin-password");
        north = createSchool("test-north", "Sunrise North");
        south = createSchool("test-south", "Sunrise South");

        String userId = JsonPath.read(mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "test-user-officer@orudesk.org", "password": "user-password"}
                                """))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.id");
        mockMvc.perform(post("/api/v1/users/{id}/roles", userId).header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role": "ADMISSIONS_OFFICER", "orgUnitId": "%s"}
                                """.formatted(north)))
                .andExpect(status().is2xxSuccessful());
        officer = login("test-user-officer@orudesk.org", "user-password");
    }

    @AfterEach
    void cleanUp() {
        jdbc.update("DELETE FROM sch_student");                 // the guardian links go with it (ON DELETE CASCADE)
        jdbc.update("DELETE FROM party_person WHERE last_name LIKE 'Test-%'");
        jdbc.update("DELETE FROM iam_user WHERE email LIKE 'test-user-%@orudesk.org'");
        jdbc.update("DELETE FROM iam_org_unit WHERE code LIKE 'test-%'");
    }

    @Test
    void anOfficerAdmitsAStudentWithTwoGuardians() throws Exception {
        String id = admitted(admit(officer, north, "2026/001", """
                [{"firstName": "Lakshmi", "lastName": "Test-Nair", "phone": "+91 98860 00001",
                  "relationship": "MOTHER", "primary": true},
                 {"firstName": "Suresh", "lastName": "Test-Nair", "relationship": "FATHER"}]
                """));

        mockMvc.perform(get("/api/v1/students/{id}", id).header("Authorization", "Bearer " + officer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Anika"))
                .andExpect(jsonPath("$.schoolId").value(north))
                .andExpect(jsonPath("$.className").value("5"))
                .andExpect(jsonPath("$.guardians[*].firstName").value(containsInAnyOrder("Lakshmi", "Suresh")))
                .andExpect(jsonPath("$.guardians[?(@.primary == true)].relationship").value(contains("MOTHER")));
    }

    @Test
    void anAdmissionIsInTheAuditLogWithWhoDidIt() throws Exception {
        String id = admitted(admit(officer, north, "2026/007", motherOnly()));

        mockMvc.perform(get("/api/v1/audit-entries").param("subjectId", id).header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].action").value("admissions.student.admitted"))
                .andExpect(jsonPath("$.items[0].summary").value("Admitted Anika Test-Nair to class 5 (no. 2026/007)"))
                .andExpect(jsonPath("$.items[0].actorEmail").value("test-user-officer@orudesk.org"));
    }

    @Test
    void anOfficerMovesAStudentToAnotherSectionAndCorrectsTheName() throws Exception {
        String id = admitted(admit(officer, north, "2026/011", motherOnly()));

        edit(officer, id, 0, 0, "Anikha", "5", "B")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Anikha"))
                .andExpect(jsonPath("$.section").value("B"))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.personVersion").value(1));

        mockMvc.perform(get("/api/v1/audit-entries").param("subjectId", id).header("Authorization", "Bearer " + admin))
                .andExpect(jsonPath("$.items[0].action").value("admissions.student.edited"))
                .andExpect(jsonPath("$.items[0].summary").value("Changed section of student no. 2026/011"))
                .andExpect(jsonPath("$.items[0].changes[0].before").value("A"))
                .andExpect(jsonPath("$.items[0].changes[0].after").value("B"));
    }

    @Test
    void aSecondEditOnTheSameVersionIsRefused() throws Exception {
        String id = admitted(admit(officer, north, "2026/012", motherOnly()));
        edit(officer, id, 0, 0, "Anika", "6", "A").andExpect(status().isOk());

        edit(admin, id, 0, 0, "Anika", "5", "C")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.currentVersion").value(1));
    }

    @Test
    void anOfficerAtSouthCannotEditANorthStudent() throws Exception {
        String id = admitted(admit(admin, north, "2026/013", motherOnly()));
        String southOfficer = officerAt(south, "test-user-south@orudesk.org");

        edit(southOfficer, id, 0, 0, "Anika", "5", "B").andExpect(status().isNotFound());
    }

    @Test
    void aSiblingKeepsTheSameGuardian() throws Exception {
        String first = admitted(admit(officer, north, "2026/001", """
                [{"firstName": "Lakshmi", "lastName": "Test-Nair", "relationship": "MOTHER", "primary": true}]
                """));
        String mother = JsonPath.read(mockMvc.perform(get("/api/v1/students/{id}", first)
                        .header("Authorization", "Bearer " + officer))
                .andReturn().getResponse().getContentAsString(), "$.guardians[0].personId");

        admitted(admit(officer, north, "2026/002", """
                [{"existingPersonId": "%s", "relationship": "MOTHER", "primary": true}]
                """.formatted(mother)));

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM party_person WHERE first_name = 'Lakshmi'",
                Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sch_student_guardian WHERE guardian_person_id = ?::uuid",
                Integer.class, mother)).isEqualTo(2);
    }

    @Test
    void anOfficerAtNorthCannotAdmitAtSouth() throws Exception {
        admit(officer, south, "2026/001", motherOnly())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Not allowed at this school"));
    }

    @Test
    void anOfficerListsOnlyTheirSchoolAndAnAdminListsBoth() throws Exception {
        admitted(admit(officer, north, "N-1", motherOnly()));
        admitted(admit(admin, south, "S-1", motherOnly()));

        mockMvc.perform(get("/api/v1/students").header("Authorization", "Bearer " + officer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].admissionNumber").value(contains("N-1")));
        mockMvc.perform(get("/api/v1/students").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].admissionNumber").value(containsInAnyOrder("N-1", "S-1")));
    }

    @Test
    void aStudentAtAnotherSchoolIsNotFound() throws Exception {
        String atSouth = admitted(admit(admin, south, "S-1", motherOnly()));

        mockMvc.perform(get("/api/v1/students/{id}", atSouth).header("Authorization", "Bearer " + officer))
                .andExpect(status().isNotFound());
    }

    @Test
    void aTakenAdmissionNumberIsRefusedAndNobodyIsAddedToTheRegistry() throws Exception {
        admitted(admit(officer, north, "2026/001", motherOnly()));
        Integer people = jdbc.queryForObject("SELECT COUNT(*) FROM party_person", Integer.class);

        admit(officer, north, "2026/001", motherOnly())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Admission number taken"));

        // The second child and their mother were inserted, then the student failed: all rolled back
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM party_person", Integer.class)).isEqualTo(people);
    }

    @Test
    void anotherSchoolMayUseTheSameNumber() throws Exception {
        admitted(admit(admin, north, "2026/001", motherOnly()));
        admitted(admit(admin, south, "2026/001", motherOnly()));
    }

    @Test
    void twoPrimaryContactsAreRefused() throws Exception {
        admit(officer, north, "2026/001", """
                [{"firstName": "Lakshmi", "lastName": "Test-Nair", "relationship": "MOTHER", "primary": true},
                 {"firstName": "Suresh", "lastName": "Test-Nair", "relationship": "FATHER", "primary": true}]
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("exactly one guardian must be the primary contact"));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM party_person WHERE last_name = 'Test-Nair'",
                Integer.class)).isZero();
    }

    @Test
    void aGuardianNeedsAnIdOrAName() throws Exception {
        admit(officer, north, "2026/001", """
                [{"relationship": "MOTHER", "primary": true}]
                """)
                .andExpect(status().isBadRequest())
                // The key the web form puts the message under: the guardian's row, the rule's name
                .andExpect(jsonPath("$.errors['guardians[0].identified']")
                        .value("give existingPersonId, or firstName and lastName"));
    }

    @Test
    void theSchoolHeaderIsRequired() throws Exception {
        mockMvc.perform(post("/api/v1/admissions").header("Authorization", "Bearer " + officer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("2026/001", motherOnly())))
                .andExpect(status().isBadRequest());
    }

    private ResultActions edit(String token, String id, int version, int personVersion, String firstName,
                               String className, String section) throws Exception {
        return mockMvc.perform(put("/api/v1/students/{id}", id).header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"version": %d, "personVersion": %d, "firstName": "%s", "lastName": "Test-Nair",
                         "dateOfBirth": "2016-06-01", "className": "%s", "section": "%s"}
                        """.formatted(version, personVersion, firstName, className, section)));
    }

    private String officerAt(String school, String email) throws Exception {
        String userId = JsonPath.read(mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"" + email + "\", \"password\": \"user-password\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.id");
        mockMvc.perform(post("/api/v1/users/{id}/roles", userId).header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\": \"ADMISSIONS_OFFICER\", \"orgUnitId\": \"" + school + "\"}"))
                .andExpect(status().is2xxSuccessful());
        return login(email, "user-password");
    }

    private static String motherOnly() {
        return """
                [{"firstName": "Lakshmi", "lastName": "Test-Nair", "relationship": "MOTHER", "primary": true}]
                """;
    }

    private static String body(String admissionNumber, String guardians) {
        return """
                {"firstName": "Anika", "lastName": "Test-Nair", "dateOfBirth": "2016-06-01",
                 "admissionNumber": "%s", "className": "5", "section": "A", "guardians": %s}
                """.formatted(admissionNumber, guardians);
    }

    private ResultActions admit(String token, String school, String admissionNumber, String guardians)
            throws Exception {
        return mockMvc.perform(post("/api/v1/admissions").header("Authorization", "Bearer " + token)
                .header("X-Org-Unit", school)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(admissionNumber, guardians)));
    }

    private static String admitted(ResultActions result) throws Exception {
        return JsonPath.read(result.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(),
                "$.id");
    }

    private String createSchool(String code, String name) throws Exception {
        return JsonPath.read(mockMvc.perform(post("/api/v1/org-units").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"parentId": "%s", "type": "SCHOOL", "code": "%s", "name": "%s"}
                                """.formatted(ROOT, code, name)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.id");
    }

    private String login(String email, String password) throws Exception {
        String json = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.accessToken");
    }
}
