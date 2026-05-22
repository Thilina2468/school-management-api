package com.campus.schoolmanagementapi.controller;

import com.campus.schoolmanagementapi.dto.LoginRequestDTO;
import com.campus.schoolmanagementapi.dto.LoginResponseDTO;
import com.campus.schoolmanagementapi.dto.StudentRegistrationDTO;
import com.campus.schoolmanagementapi.dto.StudentResponseDTO;
import com.campus.schoolmanagementapi.dto.TeacherRegistrationDTO;
import com.campus.schoolmanagementapi.dto.TeacherResponseDTO;
import com.campus.schoolmanagementapi.response.ApiResponse;
import com.campus.schoolmanagementapi.service.LoginService;
import com.campus.schoolmanagementapi.service.RegisterService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterService registerService;
    private final LoginService loginService;

    public AuthController(RegisterService registerService, LoginService loginService) {
        this.registerService = registerService;
        this.loginService = loginService;
    }

    @PostMapping("/register/student")
    public ResponseEntity<ApiResponse> registerStudent(@Valid @RequestBody StudentRegistrationDTO dto) {
        log.info("POST /auth/register/student | email: {}", dto.getEmail());
        String error = registerService.checkDuplicateEmail(dto.getEmail());
        if (error != null) {
            log.warn("POST /auth/register/student | {} -> 409", dto.getEmail());
            return ApiResponse.send(ApiResponse.CONFLICT, error, null);
        }
        StudentResponseDTO student = registerService.registerStudent(dto);
        log.info("POST /auth/register/student | {} -> 201", student.getStudentCode());
        return ApiResponse.send(ApiResponse.CREATED, ApiResponse.STUDENT_CREATED, student);
    }

    @PostMapping("/register/teacher")
    public ResponseEntity<ApiResponse> registerTeacher(@Valid @RequestBody TeacherRegistrationDTO dto) {
        log.info("POST /auth/register/teacher | email: {}", dto.getEmail());
        String error = registerService.checkDuplicateEmail(dto.getEmail());
        if (error != null) {
            log.warn("POST /auth/register/teacher | {} -> 409", dto.getEmail());
            return ApiResponse.send(ApiResponse.CONFLICT, error, null);
        }
        TeacherResponseDTO teacher = registerService.registerTeacher(dto);
        log.info("POST /auth/register/teacher | {} -> 201", teacher.getTeacherCode());
        return ApiResponse.send(ApiResponse.CREATED, ApiResponse.TEACHER_CREATED, teacher);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequestDTO dto) {
        log.info("POST /auth/login | email: {}", dto.getEmail());
        LoginResponseDTO result = loginService.login(dto);
        if (result == null) {
            log.warn("POST /auth/login | {} -> 401", dto.getEmail());
            return ApiResponse.send(ApiResponse.UNAUTHORIZED, ApiResponse.INVALID_CREDENTIALS, null);
        }
        log.info("POST /auth/login | {} -> 200", dto.getEmail());
        return ApiResponse.send(ApiResponse.OK, ApiResponse.LOGIN_SUCCESS, result);
    }

}
