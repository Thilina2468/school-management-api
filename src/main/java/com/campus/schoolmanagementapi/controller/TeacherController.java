package com.campus.schoolmanagementapi.controller;

import com.campus.schoolmanagementapi.dto.TeacherResponseDTO;
import com.campus.schoolmanagementapi.dto.TeacherUpdateDTO;
import com.campus.schoolmanagementapi.dto.TokenData;
import com.campus.schoolmanagementapi.response.ApiResponse;
import com.campus.schoolmanagementapi.service.TeacherService;
import com.campus.schoolmanagementapi.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/teachers")
public class TeacherController {

    private final TeacherService teacherService;
    private final JwtUtil jwtUtil;

    public TeacherController(TeacherService teacherService, JwtUtil jwtUtil) {
        this.teacherService = teacherService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAll() {
        log.info("GET /teachers");
        List<TeacherResponseDTO> teachers = teacherService.getAll();
        if (teachers.isEmpty()) {
            return ApiResponse.send(ApiResponse.NOT_FOUND, ApiResponse.NO_TEACHERS, null);
        }
        log.info("GET /teachers | -> 200 ({} results)", teachers.size());
        return ApiResponse.send(ApiResponse.OK, ApiResponse.TEACHERS_FETCHED, teachers);
    }

    @GetMapping("/{teacherCode}")
    public ResponseEntity<ApiResponse> getByCode(@PathVariable String teacherCode) {
        log.info("GET /teachers/{}", teacherCode);
        TeacherResponseDTO teacher = teacherService.getByCode(teacherCode);
        if (teacher == null) {
            return ApiResponse.send(ApiResponse.NOT_FOUND, ApiResponse.TEACHER_NOT_FOUND, null);
        }
        return ApiResponse.send(ApiResponse.OK, ApiResponse.TEACHER_FETCHED, teacher);
    }

    @PutMapping("/{teacherCode}")
    public ResponseEntity<ApiResponse> update(@PathVariable String teacherCode,
                                              @RequestHeader("Authorization") String authHeader,
                                              @Valid @RequestBody TeacherUpdateDTO dto) {
        TokenData tokenData = jwtUtil.extractFromHeader(authHeader);
        log.info("PUT /teachers/{} | by: {}", teacherCode, tokenData.getEmail());

        if (!tokenData.hasPermission("UPDATE_OWN_PROFILE")) {
            log.warn("PUT /teachers/{} | {} -> 403", teacherCode, tokenData.getEmail());
            return ApiResponse.send(ApiResponse.FORBIDDEN, ApiResponse.ACCESS_DENIED, null);
        }

        TeacherResponseDTO current = teacherService.getByCode(teacherCode);
        if (current == null || !current.getEmail().equals(tokenData.getEmail())) {
            log.warn("PUT /teachers/{} | {} -> 403 not owner", teacherCode, tokenData.getEmail());
            return ApiResponse.send(ApiResponse.FORBIDDEN, ApiResponse.ACCESS_DENIED, null);
        }

        String error = teacherService.updateTeacher(teacherCode, dto);
        if (error != null) {
            if (error.equals(ApiResponse.TEACHER_NOT_FOUND)) {
                return ApiResponse.send(ApiResponse.NOT_FOUND, error, null);
            }
            return ApiResponse.send(ApiResponse.CONFLICT, error, null);
        }

        TeacherResponseDTO updated = teacherService.getByCode(teacherCode);
        log.info("PUT /teachers/{} | -> 200", teacherCode);
        return ApiResponse.send(ApiResponse.OK, ApiResponse.TEACHER_UPDATED, updated);
    }

    @GetMapping("/{teacherCode}/modules")
    public ResponseEntity<ApiResponse> getModules(@PathVariable String teacherCode,
                                                  @RequestHeader("Authorization") String authHeader) {
        TokenData tokenData = jwtUtil.extractFromHeader(authHeader);
        log.info("GET /teachers/{}/modules | by: {}", teacherCode, tokenData.getEmail());

        if (!tokenData.hasPermission("VIEW_OWN_MODULES")) {
            log.warn("GET /teachers/{}/modules | {} -> 403", teacherCode, tokenData.getEmail());
            return ApiResponse.send(ApiResponse.FORBIDDEN, ApiResponse.ACCESS_DENIED, null);
        }

        TeacherResponseDTO current = teacherService.getByCode(teacherCode);
        if (current == null || !current.getEmail().equals(tokenData.getEmail())) {
            log.warn("GET /teachers/{}/modules | {} -> 403 not owner", teacherCode, tokenData.getEmail());
            return ApiResponse.send(ApiResponse.FORBIDDEN, ApiResponse.ACCESS_DENIED, null);
        }

        return ApiResponse.send(ApiResponse.OK, ApiResponse.TEACHER_FETCHED, current.getModules());
    }

}
