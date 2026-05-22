package com.campus.schoolmanagementapi.controller;

import com.campus.schoolmanagementapi.dto.MarkRequestDTO;
import com.campus.schoolmanagementapi.dto.MarkResponseDTO;
import com.campus.schoolmanagementapi.dto.TokenData;
import com.campus.schoolmanagementapi.response.ApiResponse;
import com.campus.schoolmanagementapi.service.MarkService;
import com.campus.schoolmanagementapi.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/marks")
public class MarkController {

    private final MarkService markService;
    private final JwtUtil jwtUtil;

    public MarkController(MarkService markService, JwtUtil jwtUtil) {
        this.markService = markService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> create(@RequestHeader("Authorization") String authHeader,
                                              @Valid @RequestBody MarkRequestDTO dto) {
        TokenData tokenData = jwtUtil.extractFromHeader(authHeader);
        log.info("POST /marks | by: {} for: {}", tokenData.getEmail(), dto.getStudentCode());

        if (!tokenData.hasPermission("CREATE_MARK")) {
            log.warn("POST /marks | {} -> 403", tokenData.getEmail());
            return ApiResponse.send(ApiResponse.FORBIDDEN, ApiResponse.ACCESS_DENIED, null);
        }

        String teacherCode = markService.getTeacherCode(tokenData.getEmail());
        Object result = markService.createMark(dto, teacherCode);
        if (result instanceof String error) {
            if (error.equals(ApiResponse.MARK_EXISTS)) {
                log.warn("POST /marks | {} -> 409 duplicate", dto.getStudentCode());
                return ApiResponse.send(ApiResponse.CONFLICT, error, null);
            }
            return ApiResponse.send(ApiResponse.NOT_FOUND, error, null);
        }
        log.info("POST /marks | {} -> 201", dto.getStudentCode());
        return ApiResponse.send(ApiResponse.CREATED, ApiResponse.MARK_CREATED, result);
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAll(@RequestHeader("Authorization") String authHeader) {
        TokenData tokenData = jwtUtil.extractFromHeader(authHeader);
        log.info("GET /marks | by: {}", tokenData.getEmail());

        if (!tokenData.hasPermission("VIEW_STUDENT_MARKS")) {
            log.warn("GET /marks | {} -> 403", tokenData.getEmail());
            return ApiResponse.send(ApiResponse.FORBIDDEN, ApiResponse.ACCESS_DENIED, null);
        }

        List<MarkResponseDTO> marks = markService.getAll();
        if (marks.isEmpty()) {
            return ApiResponse.send(ApiResponse.NOT_FOUND, ApiResponse.NO_MARKS, null);
        }
        log.info("GET /marks | -> 200 ({} results)", marks.size());
        return ApiResponse.send(ApiResponse.OK, ApiResponse.MARKS_FETCHED, marks);
    }

    @GetMapping("/{studentCode}")
    public ResponseEntity<ApiResponse> getByStudentCode(@PathVariable String studentCode,
                                                        @RequestHeader("Authorization") String authHeader) {
        TokenData tokenData = jwtUtil.extractFromHeader(authHeader);
        log.info("GET /marks/{} | by: {}", studentCode, tokenData.getEmail());

        if (!tokenData.hasPermission("VIEW_OWN_MARKS") && !tokenData.hasPermission("VIEW_STUDENT_MARKS")) {
            log.warn("GET /marks/{} | {} -> 403", studentCode, tokenData.getEmail());
            return ApiResponse.send(ApiResponse.FORBIDDEN, ApiResponse.ACCESS_DENIED, null);
        }

        MarkResponseDTO mark = markService.getByStudentCode(studentCode);
        if (mark == null) {
            return ApiResponse.send(ApiResponse.NOT_FOUND, ApiResponse.MARK_NOT_FOUND, null);
        }

        if (!tokenData.hasPermission("VIEW_STUDENT_MARKS")) {
            String studentEmail = markService.getStudentEmailByCode(studentCode);
            if (!tokenData.getEmail().equals(studentEmail)) {
                log.warn("GET /marks/{} | {} -> 403 cross-access", studentCode, tokenData.getEmail());
                return ApiResponse.send(ApiResponse.FORBIDDEN, ApiResponse.ACCESS_DENIED, null);
            }
        }

        return ApiResponse.send(ApiResponse.OK, ApiResponse.MARK_FETCHED, mark);
    }

    @PutMapping("/{studentCode}")
    public ResponseEntity<ApiResponse> update(@PathVariable String studentCode,
                                              @RequestHeader("Authorization") String authHeader,
                                              @Valid @RequestBody MarkRequestDTO dto) {
        TokenData tokenData = jwtUtil.extractFromHeader(authHeader);
        log.info("PUT /marks/{} | by: {}", studentCode, tokenData.getEmail());

        if (!tokenData.hasPermission("UPDATE_MARK")) {
            log.warn("PUT /marks/{} | {} -> 403", studentCode, tokenData.getEmail());
            return ApiResponse.send(ApiResponse.FORBIDDEN, ApiResponse.ACCESS_DENIED, null);
        }

        String teacherCode = markService.getTeacherCode(tokenData.getEmail());
        Object result = markService.updateMark(studentCode, dto, teacherCode);
        if (result instanceof String) {
            return ApiResponse.send(ApiResponse.NOT_FOUND, (String) result, null);
        }
        log.info("PUT /marks/{} | -> 200", studentCode);
        return ApiResponse.send(ApiResponse.OK, ApiResponse.MARK_UPDATED, result);
    }

    @DeleteMapping("/{studentCode}")
    public ResponseEntity<ApiResponse> delete(@PathVariable String studentCode,
                                              @RequestHeader("Authorization") String authHeader) {
        TokenData tokenData = jwtUtil.extractFromHeader(authHeader);
        log.info("DELETE /marks/{} | by: {}", studentCode, tokenData.getEmail());

        if (!tokenData.hasPermission("DELETE_MARK")) {
            log.warn("DELETE /marks/{} | {} -> 403", studentCode, tokenData.getEmail());
            return ApiResponse.send(ApiResponse.FORBIDDEN, ApiResponse.ACCESS_DENIED, null);
        }

        String error = markService.deleteMark(studentCode);
        if (error != null) {
            return ApiResponse.send(ApiResponse.NOT_FOUND, error, null);
        }
        log.info("DELETE /marks/{} | -> 200", studentCode);
        return ApiResponse.send(ApiResponse.OK, ApiResponse.MARK_DELETED, null);
    }

}
