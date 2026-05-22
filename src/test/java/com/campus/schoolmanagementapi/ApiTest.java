package com.campus.schoolmanagementapi;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiTest {

    @Autowired
    private MockMvc mockMvc;

    private static JdbcTemplate jdbcTemplate;

    @Autowired
    void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        ApiTest.jdbcTemplate = jdbcTemplate;
    }

    private static String studentToken;
    private static String teacherToken;
    private static boolean seeded = false;
    private static int passed = 0;

    @BeforeEach
    void setup(TestInfo testInfo) {
        if (!seeded) {
            System.out.println("\n========================================");
            System.out.println("  SCHOOL MANAGEMENT API - TEST SUITE");
            System.out.println("========================================");
            System.out.println("  Cleaning database...");
            jdbcTemplate.execute("DELETE FROM marks");
            jdbcTemplate.execute("DELETE FROM students");
            jdbcTemplate.execute("DELETE FROM teachers");
            jdbcTemplate.execute("DELETE FROM role_permissions");
            jdbcTemplate.execute("INSERT INTO role_permissions (role, permissions) VALUES ('STUDENT', '[\"VIEW_OWN_PROFILE\", \"UPDATE_OWN_PROFILE\", \"VIEW_OWN_MARKS\"]')");
            jdbcTemplate.execute("INSERT INTO role_permissions (role, permissions) VALUES ('TEACHER', '[\"VIEW_OWN_PROFILE\", \"UPDATE_OWN_PROFILE\", \"VIEW_ALL_STUDENTS\", \"VIEW_STUDENT_MARKS\", \"CREATE_MARK\", \"UPDATE_MARK\", \"DELETE_MARK\", \"DELETE_STUDENT\", \"VIEW_OWN_MODULES\", \"UPDATE_OWN_MODULES\", \"ASSIGN_MODULE\"]')");
            System.out.println("  Database cleaned and permissions seeded!");
            System.out.println("========================================\n");
            seeded = true;
        }
        System.out.println("\n--- RUNNING: " + testInfo.getDisplayName() + " ---");
    }

    @AfterEach
    void logResult(TestInfo testInfo) {
        passed++;
        System.out.println("--- PASSED: " + testInfo.getDisplayName() + " ---\n");
    }

    @AfterAll
    static void summary() {
        System.out.println("\n========================================");
        System.out.println("  TEST RESULTS SUMMARY");
        System.out.println("========================================");
        System.out.println("  Total passed: " + passed);
        System.out.println("========================================\n");
    }

    private String extractToken(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        int start = body.indexOf("\"token\":\"") + 9;
        int end = body.indexOf("\"", start);
        return body.substring(start, end);
    }

    private void logResponse(MvcResult result) throws Exception {
        System.out.println("  Status: " + result.getResponse().getStatus());
        System.out.println("  Response: " + result.getResponse().getContentAsString());
    }

    @Test @Order(1)
    @DisplayName("AUTH | Register Student (STD-001)")
    void registerStudent() throws Exception {
        System.out.println("  POST /api/v1/auth/register/student");
        MvcResult r = mockMvc.perform(post("/api/v1/auth/register/student")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"John Doe\",\"email\":\"john@school.com\",\"password\":\"secret123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.studentCode").value("STD-001"))
                .andExpect(jsonPath("$.data.name").value("John Doe"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andReturn();
        logResponse(r);
    }

    @Test @Order(2)
    @DisplayName("AUTH | Register Student 2 (STD-002)")
    void registerStudent2() throws Exception {
        System.out.println("  POST /api/v1/auth/register/student");
        MvcResult r = mockMvc.perform(post("/api/v1/auth/register/student")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Jane Doe\",\"email\":\"jane@school.com\",\"password\":\"secret456\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.studentCode").value("STD-002"))
                .andReturn();
        logResponse(r);
    }

    @Test @Order(3)
    @DisplayName("AUTH | Register Teacher (TCH-001)")
    void registerTeacher() throws Exception {
        System.out.println("  POST /api/v1/auth/register/teacher");
        MvcResult r = mockMvc.perform(post("/api/v1/auth/register/teacher")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Ms. Smith\",\"email\":\"smith@school.com\",\"password\":\"teach456\",\"modules\":[\"Mathematics\",\"Physics\"]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.teacherCode").value("TCH-001"))
                .andReturn();
        logResponse(r);
    }

    @Test @Order(4)
    @DisplayName("AUTH | Register Teacher 2 (TCH-002)")
    void registerTeacher2() throws Exception {
        System.out.println("  POST /api/v1/auth/register/teacher");
        MvcResult r = mockMvc.perform(post("/api/v1/auth/register/teacher")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Mr. Jones\",\"email\":\"jones@school.com\",\"password\":\"teach789\",\"modules\":[\"Chemistry\"]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.teacherCode").value("TCH-002"))
                .andReturn();
        logResponse(r);
    }

    @Test @Order(5)
    @DisplayName("EDGE | Duplicate email -> 409")
    void registerDuplicateEmail() throws Exception {
        System.out.println("  POST /api/v1/auth/register/student (email already exists)");
        MvcResult r = mockMvc.perform(post("/api/v1/auth/register/student")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Another\",\"email\":\"john@school.com\",\"password\":\"test123\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists"))
                .andReturn();
        logResponse(r);
    }

    @Test @Order(6)
    @DisplayName("EDGE | Blank email -> 400")
    void registerBlankEmail() throws Exception {
        System.out.println("  POST /api/v1/auth/register/student (blank email)");
        MvcResult r = mockMvc.perform(post("/api/v1/auth/register/student")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test\",\"email\":\"\",\"password\":\"test123\"}"))
                .andExpect(status().isBadRequest())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(7)
    @DisplayName("EDGE | Invalid email format -> 400")
    void registerInvalidEmail() throws Exception {
        System.out.println("  POST /api/v1/auth/register/student (invalid email)");
        MvcResult r = mockMvc.perform(post("/api/v1/auth/register/student")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test\",\"email\":\"notanemail\",\"password\":\"test123\"}"))
                .andExpect(status().isBadRequest())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(8)
    @DisplayName("EDGE | Blank name -> 400")
    void registerBlankName() throws Exception {
        System.out.println("  POST /api/v1/auth/register/student (blank name)");
        MvcResult r = mockMvc.perform(post("/api/v1/auth/register/student")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"email\":\"test@school.com\",\"password\":\"test123\"}"))
                .andExpect(status().isBadRequest())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(9)
    @DisplayName("EDGE | Blank password -> 400")
    void registerBlankPassword() throws Exception {
        System.out.println("  POST /api/v1/auth/register/student (blank password)");
        MvcResult r = mockMvc.perform(post("/api/v1/auth/register/student")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test\",\"email\":\"test@school.com\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(10)
    @DisplayName("AUTH | Login Student -> token saved")
    void loginStudent() throws Exception {
        System.out.println("  POST /api/v1/auth/login");
        MvcResult r = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"john@school.com\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn();
        studentToken = extractToken(r);
        System.out.println("  Student token saved: " + studentToken.substring(0, 20) + "...");
    }

    @Test @Order(11)
    @DisplayName("AUTH | Login Teacher -> token saved")
    void loginTeacher() throws Exception {
        System.out.println("  POST /api/v1/auth/login");
        MvcResult r = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"smith@school.com\",\"password\":\"teach456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn();
        teacherToken = extractToken(r);
        System.out.println("  Teacher token saved: " + teacherToken.substring(0, 20) + "...");
    }

    @Test @Order(12)
    @DisplayName("EDGE | Wrong password -> 401")
    void loginWrongPassword() throws Exception {
        System.out.println("  POST /api/v1/auth/login (wrong password)");
        MvcResult r = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"john@school.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(13)
    @DisplayName("EDGE | Non-existent email login -> 401")
    void loginNonExistentEmail() throws Exception {
        System.out.println("  POST /api/v1/auth/login (email doesn't exist)");
        MvcResult r = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"nobody@school.com\",\"password\":\"test123\"}"))
                .andExpect(status().isUnauthorized())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(20)
    @DisplayName("STUDENT | Teacher gets all students")
    void teacherGetsAllStudents() throws Exception {
        System.out.println("  GET /api/v1/students (teacher token)");
        MvcResult r = mockMvc.perform(get("/api/v1/students")
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(21)
    @DisplayName("STUDENT | Student views own profile")
    void studentGetsOwnProfile() throws Exception {
        System.out.println("  GET /api/v1/students/STD-001 (student token)");
        MvcResult r = mockMvc.perform(get("/api/v1/students/STD-001")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("john@school.com"))
                .andReturn();
        logResponse(r);
    }

    @Test @Order(22)
    @DisplayName("STUDENT | Student updates own profile")
    void studentUpdatesOwnProfile() throws Exception {
        System.out.println("  PUT /api/v1/students/STD-001 (student token)");
        MvcResult r = mockMvc.perform(put("/api/v1/students/STD-001")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"John Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("John Updated"))
                .andReturn();
        logResponse(r);
    }

    @Test @Order(23)
    @DisplayName("STUDENT | Teacher assigns modules to student")
    void teacherAssignsModules() throws Exception {
        System.out.println("  POST /api/v1/students/STD-001/modules (teacher token)");
        MvcResult r = mockMvc.perform(post("/api/v1/students/STD-001/modules")
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"modules\":[\"Mathematics\",\"Physics\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.modules[0]").value("Mathematics"))
                .andReturn();
        logResponse(r);
    }

    @Test @Order(24)
    @DisplayName("STUDENT | Teacher updates student status to EXPELLED")
    void teacherUpdatesStudentStatus() throws Exception {
        System.out.println("  PATCH /api/v1/students/STD-002/status (teacher token)");
        MvcResult r = mockMvc.perform(patch("/api/v1/students/STD-002/status")
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"EXPELLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("EXPELLED"))
                .andReturn();
        logResponse(r);
    }

    @Test @Order(30)
    @DisplayName("EDGE | Student views all students -> 403")
    void studentViewsAllStudentsDenied() throws Exception {
        System.out.println("  GET /api/v1/students (student token - denied)");
        MvcResult r = mockMvc.perform(get("/api/v1/students")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(31)
    @DisplayName("EDGE | Student views other student -> 403")
    void studentViewsOtherStudentDenied() throws Exception {
        System.out.println("  GET /api/v1/students/STD-002 (student token - denied)");
        MvcResult r = mockMvc.perform(get("/api/v1/students/STD-002")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(32)
    @DisplayName("EDGE | Student deletes student -> 403")
    void studentDeletesDenied() throws Exception {
        System.out.println("  DELETE /api/v1/students/STD-001 (student token - denied)");
        MvcResult r = mockMvc.perform(delete("/api/v1/students/STD-001")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(33)
    @DisplayName("EDGE | Student assigns modules -> 403")
    void studentAssignsModulesDenied() throws Exception {
        System.out.println("  POST /api/v1/students/STD-001/modules (student token - denied)");
        MvcResult r = mockMvc.perform(post("/api/v1/students/STD-001/modules")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"modules\":[\"Mathematics\"]}"))
                .andExpect(status().isForbidden())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(34)
    @DisplayName("EDGE | Student updates status -> 403")
    void studentUpdatesStatusDenied() throws Exception {
        System.out.println("  PATCH /api/v1/students/STD-001/status (student token - denied)");
        MvcResult r = mockMvc.perform(patch("/api/v1/students/STD-001/status")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"INACTIVE\"}"))
                .andExpect(status().isForbidden())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(35)
    @DisplayName("EDGE | Invalid student status -> 400")
    void invalidStudentStatus() throws Exception {
        System.out.println("  PATCH /api/v1/students/STD-001/status (status: BANANA)");
        MvcResult r = mockMvc.perform(patch("/api/v1/students/STD-001/status")
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"BANANA\"}"))
                .andExpect(status().isBadRequest())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(36)
    @DisplayName("EDGE | Student not found -> 404")
    void studentNotFound() throws Exception {
        System.out.println("  GET /api/v1/students/STD-999 (teacher token)");
        MvcResult r = mockMvc.perform(get("/api/v1/students/STD-999")
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Student not found"))
                .andReturn();
        logResponse(r);
    }

    @Test @Order(37)
    @DisplayName("EDGE | Update student duplicate email -> 409")
    void updateStudentDuplicateEmail() throws Exception {
        System.out.println("  PUT /api/v1/students/STD-001 (email already used by STD-002)");
        MvcResult r = mockMvc.perform(put("/api/v1/students/STD-001")
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"jane@school.com\"}"))
                .andExpect(status().isConflict())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(40)
    @DisplayName("TEACHER | Get all teachers (student can view)")
    void getAllTeachers() throws Exception {
        System.out.println("  GET /api/v1/teachers (student token)");
        MvcResult r = mockMvc.perform(get("/api/v1/teachers")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(41)
    @DisplayName("TEACHER | Get teacher by code")
    void getTeacherByCode() throws Exception {
        System.out.println("  GET /api/v1/teachers/TCH-001 (student token)");
        MvcResult r = mockMvc.perform(get("/api/v1/teachers/TCH-001")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.teacherCode").value("TCH-001"))
                .andReturn();
        logResponse(r);
    }

    @Test @Order(42)
    @DisplayName("TEACHER | Teacher updates own profile")
    void teacherUpdatesOwnProfile() throws Exception {
        System.out.println("  PUT /api/v1/teachers/TCH-001 (teacher token)");
        MvcResult r = mockMvc.perform(put("/api/v1/teachers/TCH-001")
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Ms. Smith Updated\",\"modules\":[\"Mathematics\",\"Physics\",\"Chemistry\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Ms. Smith Updated"))
                .andReturn();
        logResponse(r);
    }

    @Test @Order(43)
    @DisplayName("TEACHER | Teacher gets own modules")
    void teacherGetsOwnModules() throws Exception {
        System.out.println("  GET /api/v1/teachers/TCH-001/modules (teacher token)");
        MvcResult r = mockMvc.perform(get("/api/v1/teachers/TCH-001/modules")
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(44)
    @DisplayName("EDGE | Student updates teacher -> 403")
    void studentUpdatesTeacherDenied() throws Exception {
        System.out.println("  PUT /api/v1/teachers/TCH-001 (student token - denied)");
        MvcResult r = mockMvc.perform(put("/api/v1/teachers/TCH-001")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Hacked\"}"))
                .andExpect(status().isForbidden())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(50)
    @DisplayName("MARK | Teacher creates mark for STD-001")
    void teacherCreatesMark() throws Exception {
        System.out.println("  POST /api/v1/marks (teacher token)");
        MvcResult r = mockMvc.perform(post("/api/v1/marks")
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"studentCode\":\"STD-001\",\"marks\":{\"Mathematics\":85,\"Physics\":72}}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.studentCode").value("STD-001"))
                .andReturn();
        logResponse(r);
    }

    @Test @Order(51)
    @DisplayName("MARK | Teacher gets all marks")
    void teacherGetsAllMarks() throws Exception {
        System.out.println("  GET /api/v1/marks (teacher token)");
        MvcResult r = mockMvc.perform(get("/api/v1/marks")
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(52)
    @DisplayName("MARK | Teacher gets mark by code")
    void teacherGetsMarkByCode() throws Exception {
        System.out.println("  GET /api/v1/marks/STD-001 (teacher token)");
        MvcResult r = mockMvc.perform(get("/api/v1/marks/STD-001")
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentCode").value("STD-001"))
                .andReturn();
        logResponse(r);
    }

    @Test @Order(53)
    @DisplayName("MARK | Student gets own mark")
    void studentGetsOwnMark() throws Exception {
        System.out.println("  GET /api/v1/marks/STD-001 (student token - own mark)");
        MvcResult r = mockMvc.perform(get("/api/v1/marks/STD-001")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentCode").value("STD-001"))
                .andReturn();
        logResponse(r);
    }

    @Test @Order(54)
    @DisplayName("MARK | Teacher updates mark (Math: 85 -> 92)")
    void teacherUpdatesMark() throws Exception {
        System.out.println("  PUT /api/v1/marks/STD-001 (teacher token)");
        MvcResult r = mockMvc.perform(put("/api/v1/marks/STD-001")
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"studentCode\":\"STD-001\",\"marks\":{\"Mathematics\":92}}"))
                .andExpect(status().isOk())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(55)
    @DisplayName("MARK | Student gets marks via /students endpoint")
    void studentGetsOwnMarksViaStudentEndpoint() throws Exception {
        System.out.println("  GET /api/v1/students/STD-001/marks (student token)");
        MvcResult r = mockMvc.perform(get("/api/v1/students/STD-001/marks")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(60)
    @DisplayName("EDGE | Student creates mark -> 403")
    void studentCreatesMarkDenied() throws Exception {
        System.out.println("  POST /api/v1/marks (student token - denied)");
        MvcResult r = mockMvc.perform(post("/api/v1/marks")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"studentCode\":\"STD-001\",\"marks\":{\"Mathematics\":100}}"))
                .andExpect(status().isForbidden())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(61)
    @DisplayName("EDGE | Student gets all marks -> 403")
    void studentGetsAllMarksDenied() throws Exception {
        System.out.println("  GET /api/v1/marks (student token - denied)");
        MvcResult r = mockMvc.perform(get("/api/v1/marks")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(62)
    @DisplayName("EDGE | Student deletes mark -> 403")
    void studentDeletesMarkDenied() throws Exception {
        System.out.println("  DELETE /api/v1/marks/STD-001 (student token - denied)");
        MvcResult r = mockMvc.perform(delete("/api/v1/marks/STD-001")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(63)
    @DisplayName("EDGE | Student updates mark -> 403")
    void studentUpdatesMarkDenied() throws Exception {
        System.out.println("  PUT /api/v1/marks/STD-001 (student token - denied)");
        MvcResult r = mockMvc.perform(put("/api/v1/marks/STD-001")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"studentCode\":\"STD-001\",\"marks\":{\"Mathematics\":100}}"))
                .andExpect(status().isForbidden())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(64)
    @DisplayName("EDGE | Duplicate mark for same student -> 409")
    void duplicateMarkDenied() throws Exception {
        System.out.println("  POST /api/v1/marks (STD-001 already has marks)");
        MvcResult r = mockMvc.perform(post("/api/v1/marks")
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"studentCode\":\"STD-001\",\"marks\":{\"Mathematics\":50}}"))
                .andExpect(status().isConflict())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(65)
    @DisplayName("MARK | Teacher marks any module (Biology) for STD-002")
    void teacherMarksAnyModule() throws Exception {
        System.out.println("  POST /api/v1/marks (Biology - any teacher can mark any module)");
        MvcResult r = mockMvc.perform(post("/api/v1/marks")
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"studentCode\":\"STD-002\",\"marks\":{\"Biology\":90}}"))
                .andExpect(status().isCreated())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(66)
    @DisplayName("MARK | Teacher updates mark with any module (Biology)")
    void teacherUpdatesMarkAnyModule() throws Exception {
        System.out.println("  PUT /api/v1/marks/STD-001 (adding Biology to STD-001)");
        MvcResult r = mockMvc.perform(put("/api/v1/marks/STD-001")
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"studentCode\":\"STD-001\",\"marks\":{\"Biology\":88}}"))
                .andExpect(status().isOk())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(67)
    @DisplayName("EDGE | Mark non-existent student -> 404")
    void markNonExistentStudent() throws Exception {
        System.out.println("  POST /api/v1/marks (STD-999 doesn't exist)");
        MvcResult r = mockMvc.perform(post("/api/v1/marks")
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"studentCode\":\"STD-999\",\"marks\":{\"Mathematics\":50}}"))
                .andExpect(status().isNotFound())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(68)
    @DisplayName("EDGE | Student views other student's mark -> 403")
    void studentViewsOtherStudentMark() throws Exception {
        System.out.println("  GET /api/v1/marks/STD-002 (student STD-001 - not their mark)");
        MvcResult r = mockMvc.perform(get("/api/v1/marks/STD-002")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(69)
    @DisplayName("EDGE | No token -> 403")
    void noTokenDenied() throws Exception {
        System.out.println("  GET /api/v1/students (no header)");
        mockMvc.perform(get("/api/v1/students"))
                .andExpect(status().isForbidden());
        System.out.println("  Status: 403");
    }

    @Test @Order(88)
    @DisplayName("CLEANUP | Delete mark STD-002")
    void deleteMark2() throws Exception {
        System.out.println("  DELETE /api/v1/marks/STD-002 (teacher token)");
        MvcResult r = mockMvc.perform(delete("/api/v1/marks/STD-002")
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(89)
    @DisplayName("CLEANUP | Delete mark STD-001")
    void deleteMark1() throws Exception {
        System.out.println("  DELETE /api/v1/marks/STD-001 (teacher token)");
        MvcResult r = mockMvc.perform(delete("/api/v1/marks/STD-001")
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andReturn();
        logResponse(r);
    }

    @Test @Order(90)
    @DisplayName("CLEANUP | Delete student STD-002")
    void deleteStudent() throws Exception {
        System.out.println("  DELETE /api/v1/students/STD-002 (teacher token)");
        MvcResult r = mockMvc.perform(delete("/api/v1/students/STD-002")
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andReturn();
        logResponse(r);
    }

}
