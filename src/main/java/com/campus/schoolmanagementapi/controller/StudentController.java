package com.campus.schoolmanagementapi.controller;

import com.campus.schoolmanagementapi.dto.MarkResponseDTO;
import com.campus.schoolmanagementapi.dto.ModuleAssignDTO;
import com.campus.schoolmanagementapi.dto.StatusUpdateDTO;
import com.campus.schoolmanagementapi.dto.StudentResponseDTO;
import com.campus.schoolmanagementapi.dto.StudentUpdateDTO;
import com.campus.schoolmanagementapi.dto.TokenData;
import com.campus.schoolmanagementapi.response.ApiResponse;
import com.campus.schoolmanagementapi.service.StudentService;
import com.campus.schoolmanagementapi.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService studentService;
    private final JwtUtil jwtUtil;

    public StudentController(StudentService studentService, JwtUtil jwtUtil) {
        this.studentService = studentService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAll(@RequestHeader("Authorization") String authHeader) {
        TokenData tokenData = jwtUtil.extractFromHeader(authHeader);
        log.info("GET /students | by: {}", tokenData.getEmail());
        if (!tokenData.hasPermission("VIEW_ALL_STUDENTS")) {
            log.warn("GET /students | {} -> 403", tokenData.getEmail());
            return ApiResponse.send(ApiResponse.FORBIDDEN, ApiResponse.ACCESS_DENIED, null);
        }

        List<StudentResponseDTO> students = studentService.getAll();
        if (students.isEmpty()) {
            return ApiResponse.send(ApiResponse.NOT_FOUND, ApiResponse.NO_STUDENTS, null);
        }
        log.info("GET /students | -> 200 ({} results)", students.size());
        return ApiResponse.send(ApiResponse.OK, ApiResponse.STUDENTS_FETCHED, students);
    }

    @GetMapping("/{studentCode}")
    public ResponseEntity<ApiResponse> getByCode(@PathVariable String studentCode,
                                                 @RequestHeader("Authorization") String authHeader) {
        TokenData tokenData = jwtUtil.extractFromHeader(authHeader);
        log.info("GET /students/{} | by: {}", studentCode, tokenData.getEmail());

        if (!tokenData.hasPermission("VIEW_ALL_STUDENTS") && !tokenData.hasPermission("VIEW_OWN_PROFILE")) {
            log.warn("GET /students/{} | {} -> 403", studentCode, tokenData.getEmail());
            return ApiResponse.send(ApiResponse.FORBIDDEN, ApiResponse.ACCESS_DENIED, null);
        }

        StudentResponseDTO student = studentService.getByCode(studentCode);
        if (student == null) {
            return ApiResponse.send(ApiResponse.NOT_FOUND, ApiResponse.STUDENT_NOT_FOUND, null);
        }

        if (!tokenData.hasPermission("VIEW_ALL_STUDENTS")) {
            if (!student.getEmail().equals(tokenData.getEmail())) {
                log.warn("GET /students/{} | {} -> 403 cross-access", studentCode, tokenData.getEmail());
                return ApiResponse.send(ApiResponse.FORBIDDEN, ApiResponse.ACCESS_DENIED, null);
            }
        }

        return ApiResponse.send(ApiResponse.OK, ApiResponse.STUDENT_FETCHED, student);
    }

    @PutMapping("/{studentCode}")
    public ResponseEntity<ApiResponse> update(@PathVariable String studentCode,
                                              @RequestHeader("Authorization") String authHeader,
                                              @Valid @RequestBody StudentUpdateDTO dto) {
        TokenData tokenData = jwtUtil.extractFromHeader(authHeader);
        log.info("PUT /students/{} | by: {}", studentCode, tokenData.getEmail());

        if (!tokenData.hasPermission("UPDATE_OWN_PROFILE")) {
            log.warn("PUT /students/{} | {} -> 403", studentCode, tokenData.getEmail());
            return ApiResponse.send(ApiResponse.FORBIDDEN, ApiResponse.ACCESS_DENIED, null);
        }

        if ("STUDENT".equals(tokenData.getRole())) {
            StudentResponseDTO current = studentService.getByCode(studentCode);
            if (current == null || !current.getEmail().equals(tokenData.getEmail())) {
                log.warn("PUT /students/{} | {} -> 403 not owner", studentCode, tokenData.getEmail());
                return ApiResponse.send(ApiResponse.FORBIDDEN, ApiResponse.ACCESS_DENIED, null);
            }
        }

        String error = studentService.updateStudent(studentCode, dto);
        if (error != null) {
            if (error.equals(ApiResponse.STUDENT_NOT_FOUND)) {
                return ApiResponse.send(ApiResponse.NOT_FOUND, error, null);
            }
            return ApiResponse.send(ApiResponse.CONFLICT, error, null);
        }

        StudentResponseDTO updated = studentService.getByCode(studentCode);
        log.info("PUT /students/{} | -> 200", studentCode);
        return ApiResponse.send(ApiResponse.OK, ApiResponse.STUDENT_UPDATED, updated);
    }

    @DeleteMapping("/{studentCode}")
    public ResponseEntity<ApiResponse> delete(@PathVariable String studentCode,
                                              @RequestHeader("Authorization") String authHeader) {
        TokenData tokenData = jwtUtil.extractFromHeader(authHeader);
        log.info("DELETE /students/{} | by: {}", studentCode, tokenData.getEmail());
        if (!tokenData.hasPermission("DELETE_STUDENT")) {
            log.warn("DELETE /students/{} | {} -> 403", studentCode, tokenData.getEmail());
            return ApiResponse.send(ApiResponse.FORBIDDEN, ApiResponse.ACCESS_DENIED, null);
        }

        String error = studentService.deleteStudent(studentCode);
        if (error != null) {
            return ApiResponse.send(ApiResponse.NOT_FOUND, error, null);
        }
        log.info("DELETE /students/{} | -> 200", studentCode);
        return ApiResponse.send(ApiResponse.OK, ApiResponse.STUDENT_DELETED, null);
    }

    @PatchMapping("/{studentCode}/status")
    public ResponseEntity<ApiResponse> updateStatus(@PathVariable String studentCode,
                                                    @RequestHeader("Authorization") String authHeader,
                                                    @Valid @RequestBody StatusUpdateDTO dto) {
        TokenData tokenData = jwtUtil.extractFromHeader(authHeader);
        log.info("PATCH /students/{}/status | by: {} to: {}", studentCode, tokenData.getEmail(), dto.getStatus());
        if (!tokenData.hasPermission("DELETE_STUDENT")) {
            log.warn("PATCH /students/{}/status | {} -> 403", studentCode, tokenData.getEmail());
            return ApiResponse.send(ApiResponse.FORBIDDEN, ApiResponse.ACCESS_DENIED, null);
        }

        String error = studentService.updateStatus(studentCode, dto.getStatus());
        if (error != null) {
            if (error.equals(ApiResponse.STUDENT_NOT_FOUND)) {
                return ApiResponse.send(ApiResponse.NOT_FOUND, error, null);
            }
            return ApiResponse.send(ApiResponse.BAD_REQUEST, error, null);
        }

        StudentResponseDTO updated = studentService.getByCode(studentCode);
        log.info("PATCH /students/{}/status | -> 200", studentCode);
        return ApiResponse.send(ApiResponse.OK, ApiResponse.STUDENT_UPDATED, updated);
    }

    @PostMapping("/{studentCode}/modules")
    public ResponseEntity<ApiResponse> assignModules(@PathVariable String studentCode,
                                                     @RequestHeader("Authorization") String authHeader,
                                                     @Valid @RequestBody ModuleAssignDTO dto) {
        TokenData tokenData = jwtUtil.extractFromHeader(authHeader);
        log.info("POST /students/{}/modules | by: {}", studentCode, tokenData.getEmail());
        if (!tokenData.hasPermission("ASSIGN_MODULE")) {
            log.warn("POST /students/{}/modules | {} -> 403", studentCode, tokenData.getEmail());
            return ApiResponse.send(ApiResponse.FORBIDDEN, ApiResponse.ACCESS_DENIED, null);
        }

        String error = studentService.assignModules(studentCode, dto.getModules());
        if (error != null) {
            return ApiResponse.send(ApiResponse.NOT_FOUND, error, null);
        }

        StudentResponseDTO updated = studentService.getByCode(studentCode);
        log.info("POST /students/{}/modules | -> 200", studentCode);
        return ApiResponse.send(ApiResponse.OK, ApiResponse.STUDENT_UPDATED, updated);
    }

    @GetMapping("/{studentCode}/marks")
    public ResponseEntity<ApiResponse> getMarks(@PathVariable String studentCode,
                                                @RequestHeader("Authorization") String authHeader) {
        TokenData tokenData = jwtUtil.extractFromHeader(authHeader);
        log.info("GET /students/{}/marks | by: {}", studentCode, tokenData.getEmail());

        if (!tokenData.hasPermission("VIEW_OWN_MARKS") && !tokenData.hasPermission("VIEW_STUDENT_MARKS")) {
            log.warn("GET /students/{}/marks | {} -> 403", studentCode, tokenData.getEmail());
            return ApiResponse.send(ApiResponse.FORBIDDEN, ApiResponse.ACCESS_DENIED, null);
        }

        if (!tokenData.hasPermission("VIEW_STUDENT_MARKS")) {
            StudentResponseDTO current = studentService.getByCode(studentCode);
            if (current == null || !current.getEmail().equals(tokenData.getEmail())) {
                log.warn("GET /students/{}/marks | {} -> 403 cross-access", studentCode, tokenData.getEmail());
                return ApiResponse.send(ApiResponse.FORBIDDEN, ApiResponse.ACCESS_DENIED, null);
            }
        }

        List<MarkResponseDTO> marks = studentService.getStudentMarks(studentCode);
        if (marks == null) {
            return ApiResponse.send(ApiResponse.NOT_FOUND, ApiResponse.STUDENT_NOT_FOUND, null);
        }
        if (marks.isEmpty()) {
            return ApiResponse.send(ApiResponse.NOT_FOUND, ApiResponse.NO_MARKS, null);
        }
        log.info("GET /students/{}/marks | -> 200", studentCode);
        return ApiResponse.send(ApiResponse.OK, ApiResponse.MARKS_FETCHED, marks);
    }

}
