package com.campus.schoolmanagementapi.repository;

import com.campus.schoolmanagementapi.entity.Mark;
import com.campus.schoolmanagementapi.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarkRepository extends JpaRepository<Mark, Long> {

    List<Mark> findByStudent(Student student);

    Mark findByStudentStudentCode(String studentCode);

}
