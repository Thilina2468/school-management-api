package com.campus.schoolmanagementapi.repository;

import com.campus.schoolmanagementapi.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Teacher findByEmail(String email);

    Teacher findByTeacherCode(String teacherCode);

}