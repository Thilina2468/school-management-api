package com.campus.schoolmanagementapi.service;

import com.campus.schoolmanagementapi.dto.LoginRequestDTO;
import com.campus.schoolmanagementapi.dto.LoginResponseDTO;
import com.campus.schoolmanagementapi.dto.StudentResponseDTO;
import com.campus.schoolmanagementapi.dto.TeacherResponseDTO;
import com.campus.schoolmanagementapi.entity.Student;
import com.campus.schoolmanagementapi.entity.Teacher;
import com.campus.schoolmanagementapi.entity.RolePermission;
import com.campus.schoolmanagementapi.repository.StudentRepository;
import com.campus.schoolmanagementapi.repository.TeacherRepository;
import com.campus.schoolmanagementapi.repository.RolePermissionRepository;
import com.campus.schoolmanagementapi.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class LoginService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginService(StudentRepository studentRepository,
                        TeacherRepository teacherRepository,
                        RolePermissionRepository rolePermissionRepository,
                        PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponseDTO login(LoginRequestDTO dto) {
        Student student = studentRepository.findByEmail(dto.getEmail());
        if (student != null) {
            if (passwordEncoder.matches(dto.getPassword(), student.getPasswordHash())) {
                log.info("Login successful: {} (STUDENT)", dto.getEmail());
                return generateResponse(student.getEmail(), "STUDENT");
            }
            log.warn("Login failed: invalid password for {}", dto.getEmail());
            return null;
        }

        Teacher teacher = teacherRepository.findByEmail(dto.getEmail());
        if (teacher != null) {
            if (passwordEncoder.matches(dto.getPassword(), teacher.getPasswordHash())) {
                log.info("Login successful: {} (TEACHER)", dto.getEmail());
                return generateResponse(teacher.getEmail(), "TEACHER");
            }
            log.warn("Login failed: invalid password for {}", dto.getEmail());
            return null;
        }
        log.warn("Login failed: email not found {}", dto.getEmail());
        return null;
    }

    private LoginResponseDTO generateResponse(String email, String role) {
        RolePermission rolePermission = rolePermissionRepository.findByRole(role);
        List<String> permissions = rolePermission != null ? rolePermission.getPermissions() : new ArrayList<>();

        String token = jwtUtil.generateToken(email, role, permissions);

        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);
        return response;
    }

}
