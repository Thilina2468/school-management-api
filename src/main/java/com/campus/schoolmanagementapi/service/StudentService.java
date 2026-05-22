package com.campus.schoolmanagementapi.service;

import com.campus.schoolmanagementapi.dto.MarkResponseDTO;
import com.campus.schoolmanagementapi.dto.StudentResponseDTO;
import com.campus.schoolmanagementapi.dto.StudentUpdateDTO;
import com.campus.schoolmanagementapi.entity.Student;
import com.campus.schoolmanagementapi.enums.StudentStatus;
import com.campus.schoolmanagementapi.repository.MarkRepository;
import com.campus.schoolmanagementapi.repository.StudentRepository;
import com.campus.schoolmanagementapi.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final MarkRepository markRepository;
    private final MarkService markService;

    public StudentService(StudentRepository studentRepository,
                          MarkRepository markRepository,
                          MarkService markService) {
        this.studentRepository = studentRepository;
        this.markRepository = markRepository;
        this.markService = markService;
    }

    public StudentResponseDTO getByCode(String studentCode) {
        Student student = studentRepository.findByStudentCode(studentCode);
        if (student == null) return null;
        return toResponse(student);
    }

    public List<StudentResponseDTO> getAll() {
        return studentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public String updateStudent(String studentCode, StudentUpdateDTO dto) {
        Student student = studentRepository.findByStudentCode(studentCode);
        if (student == null) {
            log.warn("Student not found: {}", studentCode);
            return ApiResponse.STUDENT_NOT_FOUND;
        }

        if (dto.getName() != null && !dto.getName().isBlank()) {
            student.setName(dto.getName());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            Student existing = studentRepository.findByEmail(dto.getEmail());
            if (existing != null && !existing.getStudentCode().equals(studentCode)) {
                return ApiResponse.DUPLICATE_EMAIL;
            }
            student.setEmail(dto.getEmail());
        }

        studentRepository.save(student);
        log.info("Student updated: {}", studentCode);
        return null;
    }

    public String updateStatus(String studentCode, String status) {
        Student student = studentRepository.findByStudentCode(studentCode);
        if (student == null) return ApiResponse.STUDENT_NOT_FOUND;

        try {
            student.setStatus(StudentStatus.valueOf(status));
        } catch (IllegalArgumentException e) {
            return "Invalid status. Must be ACTIVE, EXPELLED, or INACTIVE";
        }

        studentRepository.save(student);
        return null;
    }

    public String deleteStudent(String studentCode) {
        Student student = studentRepository.findByStudentCode(studentCode);
        if (student == null) return ApiResponse.STUDENT_NOT_FOUND;
        studentRepository.delete(student);
        log.info("Student deleted: {}", studentCode);
        return null;
    }

    public List<MarkResponseDTO> getStudentMarks(String studentCode) {
        Student student = studentRepository.findByStudentCode(studentCode);
        if (student == null) return null;
        return markRepository.findByStudent(student)
                .stream()
                .map(markService::toResponse)
                .toList();
    }

    public String assignModules(String studentCode, List<String> modules) {
        Student student = studentRepository.findByStudentCode(studentCode);
        if (student == null) return ApiResponse.STUDENT_NOT_FOUND;
        student.setModules(modules);
        studentRepository.save(student);
        log.info("Modules assigned to {}: {}", studentCode, modules);
        return null;
    }

    public StudentResponseDTO toResponse(Student student) {
        StudentResponseDTO response = new StudentResponseDTO();
        response.setStudentCode(student.getStudentCode());
        response.setName(student.getName());
        response.setEmail(student.getEmail());
        response.setStatus(student.getStatus().name());
        response.setModules(student.getModules());
        response.setCreatedAt(student.getCreatedAt());
        response.setUpdatedAt(student.getUpdatedAt());
        return response;
    }

}
