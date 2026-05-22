package com.campus.schoolmanagementapi.service;

import com.campus.schoolmanagementapi.dto.MarkRequestDTO;
import com.campus.schoolmanagementapi.dto.MarkResponseDTO;
import com.campus.schoolmanagementapi.entity.Mark;
import com.campus.schoolmanagementapi.entity.Student;
import com.campus.schoolmanagementapi.entity.Teacher;
import com.campus.schoolmanagementapi.repository.MarkRepository;
import com.campus.schoolmanagementapi.repository.StudentRepository;
import com.campus.schoolmanagementapi.repository.TeacherRepository;
import com.campus.schoolmanagementapi.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MarkService {

    private final MarkRepository markRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    public MarkService(MarkRepository markRepository,
                       StudentRepository studentRepository,
                       TeacherRepository teacherRepository) {
        this.markRepository = markRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
    }

    public String getTeacherCode(String teacherEmail) {
        Teacher teacher = teacherRepository.findByEmail(teacherEmail);
        return teacher != null ? teacher.getTeacherCode() : null;
    }

    public String getStudentEmailByCode(String studentCode) {
        Student student = studentRepository.findByStudentCode(studentCode);
        return student != null ? student.getEmail() : null;
    }

    public Object createMark(MarkRequestDTO dto, String teacherCode) {
        Student student = studentRepository.findByStudentCode(dto.getStudentCode());
        if (student == null) return ApiResponse.STUDENT_NOT_FOUND;

        Mark existing = markRepository.findByStudentStudentCode(dto.getStudentCode());
        if (existing != null) return ApiResponse.MARK_EXISTS;

        Mark mark = new Mark();
        mark.setStudent(student);

        Map<String, Object> marksMap = new HashMap<>();
        for (Map.Entry<String, Double> entry : dto.getMarks().entrySet()) {
            Map<String, Object> moduleData = new HashMap<>();
            moduleData.put("score", entry.getValue());
            moduleData.put("updatedBy", teacherCode);
            marksMap.put(entry.getKey(), moduleData);
        }
        mark.setMarks(marksMap);

        Mark saved = markRepository.save(mark);
        log.info("Mark created for student {}", dto.getStudentCode());
        return toResponse(saved);
    }

    public Object updateMark(String studentCode, MarkRequestDTO dto, String teacherCode) {
        Mark mark = markRepository.findByStudentStudentCode(studentCode);
        if (mark == null) return ApiResponse.MARK_NOT_FOUND;

        Map<String, Object> existingMarks = mark.getMarks();
        for (Map.Entry<String, Double> entry : dto.getMarks().entrySet()) {
            Map<String, Object> moduleData = new HashMap<>();
            moduleData.put("score", entry.getValue());
            moduleData.put("updatedBy", teacherCode);
            existingMarks.put(entry.getKey(), moduleData);
        }
        mark.setMarks(existingMarks);

        Mark saved = markRepository.save(mark);
        log.info("Mark updated for student {}", studentCode);
        return toResponse(saved);
    }

    public MarkResponseDTO getByStudentCode(String studentCode) {
        Mark mark = markRepository.findByStudentStudentCode(studentCode);
        if (mark == null) return null;
        return toResponse(mark);
    }

    public List<MarkResponseDTO> getAll() {
        return markRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public String deleteMark(String studentCode) {
        Mark mark = markRepository.findByStudentStudentCode(studentCode);
        if (mark == null) return ApiResponse.MARK_NOT_FOUND;
        markRepository.delete(mark);
        log.info("Mark deleted for student {}", studentCode);
        return null;
    }

    public MarkResponseDTO toResponse(Mark mark) {
        MarkResponseDTO response = new MarkResponseDTO();
        response.setStudentCode(mark.getStudent().getStudentCode());
        response.setMarks(mark.getMarks());
        response.setCreatedAt(mark.getCreatedAt());
        response.setUpdatedAt(mark.getUpdatedAt());
        return response;
    }

}
