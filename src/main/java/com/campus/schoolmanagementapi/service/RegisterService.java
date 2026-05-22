package com.campus.schoolmanagementapi.service;

import com.campus.schoolmanagementapi.dto.StudentRegistrationDTO;
import com.campus.schoolmanagementapi.dto.StudentResponseDTO;
import com.campus.schoolmanagementapi.dto.TeacherRegistrationDTO;
import com.campus.schoolmanagementapi.dto.TeacherResponseDTO;
import com.campus.schoolmanagementapi.entity.Student;
import com.campus.schoolmanagementapi.entity.Teacher;
import com.campus.schoolmanagementapi.repository.StudentRepository;
import com.campus.schoolmanagementapi.repository.TeacherRepository;
import com.campus.schoolmanagementapi.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RegisterService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentService studentService;
    private final TeacherService teacherService;

    public RegisterService(StudentRepository studentRepository,
                           TeacherRepository teacherRepository,
                           PasswordEncoder passwordEncoder,
                           StudentService studentService,
                           TeacherService teacherService) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.passwordEncoder = passwordEncoder;
        this.studentService = studentService;
        this.teacherService = teacherService;
    }

    public String checkDuplicateEmail(String email) {
        if (studentRepository.findByEmail(email) != null) {
            log.warn("Duplicate email attempt: {}", email);
            return ApiResponse.DUPLICATE_EMAIL;
        }
        if (teacherRepository.findByEmail(email) != null) {
            log.warn("Duplicate email attempt: {}", email);
            return ApiResponse.DUPLICATE_EMAIL;
        }
        return null;
    }

    private String generateStudentCode() {
        long count = studentRepository.count() + 1;
        return String.format("STD-%03d", count);
    }

    private String generateTeacherCode() {
        long count = teacherRepository.count() + 1;
        return String.format("TCH-%03d", count);
    }

    public StudentResponseDTO registerStudent(StudentRegistrationDTO dto) {
        Student student = new Student();
        student.setStudentCode(generateStudentCode());
        student.setName(dto.getName());
        student.setEmail(dto.getEmail());
        student.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        Student saved = studentRepository.save(student);
        log.info("Student registered: {} ({})", saved.getStudentCode(), saved.getEmail());
        return studentService.toResponse(saved);
    }

    public TeacherResponseDTO registerTeacher(TeacherRegistrationDTO dto) {
        Teacher teacher = new Teacher();
        teacher.setTeacherCode(generateTeacherCode());
        teacher.setName(dto.getName());
        teacher.setEmail(dto.getEmail());
        teacher.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        teacher.setModules(dto.getModules());

        Teacher saved = teacherRepository.save(teacher);
        log.info("Teacher registered: {} ({})", saved.getTeacherCode(), saved.getEmail());
        return teacherService.toResponse(saved);
    }

}
