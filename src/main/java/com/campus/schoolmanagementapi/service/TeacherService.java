package com.campus.schoolmanagementapi.service;

import com.campus.schoolmanagementapi.dto.TeacherResponseDTO;
import com.campus.schoolmanagementapi.dto.TeacherUpdateDTO;
import com.campus.schoolmanagementapi.entity.Teacher;
import com.campus.schoolmanagementapi.repository.TeacherRepository;
import com.campus.schoolmanagementapi.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;

    public TeacherService(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    public TeacherResponseDTO getByCode(String teacherCode) {
        Teacher teacher = teacherRepository.findByTeacherCode(teacherCode);
        if (teacher == null) return null;
        return toResponse(teacher);
    }

    public List<TeacherResponseDTO> getAll() {
        return teacherRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public String updateTeacher(String teacherCode, TeacherUpdateDTO dto) {
        Teacher teacher = teacherRepository.findByTeacherCode(teacherCode);
        if (teacher == null) {
            log.warn("Teacher not found: {}", teacherCode);
            return ApiResponse.TEACHER_NOT_FOUND;
        }

        if (dto.getName() != null && !dto.getName().isBlank()) {
            teacher.setName(dto.getName());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            Teacher existing = teacherRepository.findByEmail(dto.getEmail());
            if (existing != null && !existing.getTeacherCode().equals(teacherCode)) {
                return ApiResponse.DUPLICATE_EMAIL;
            }
            teacher.setEmail(dto.getEmail());
        }
        if (dto.getModules() != null) {
            teacher.setModules(dto.getModules());
        }

        teacherRepository.save(teacher);
        log.info("Teacher updated: {}", teacherCode);
        return null;
    }

    public TeacherResponseDTO toResponse(Teacher teacher) {
        TeacherResponseDTO response = new TeacherResponseDTO();
        response.setTeacherCode(teacher.getTeacherCode());
        response.setName(teacher.getName());
        response.setEmail(teacher.getEmail());
        response.setModules(teacher.getModules());
        response.setCreatedAt(teacher.getCreatedAt());
        response.setUpdatedAt(teacher.getUpdatedAt());
        return response;
    }

}
