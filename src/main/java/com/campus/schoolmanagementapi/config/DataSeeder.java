package com.campus.schoolmanagementapi.config;

import com.campus.schoolmanagementapi.entity.RolePermission;
import com.campus.schoolmanagementapi.repository.RolePermissionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class DataSeeder implements CommandLineRunner {

    private final RolePermissionRepository rolePermissionRepository;

    public DataSeeder(RolePermissionRepository rolePermissionRepository) {
        this.rolePermissionRepository = rolePermissionRepository;
    }

    @Override
    public void run(String... args) {
        if (rolePermissionRepository.count() > 0) {
            log.info("Permissions already seeded, skipping");
            return;
        }

        RolePermission student = new RolePermission();
        student.setRole("STUDENT");
        student.setPermissions(List.of(
                "VIEW_OWN_PROFILE",
                "UPDATE_OWN_PROFILE",
                "VIEW_OWN_MARKS"
        ));

        RolePermission teacher = new RolePermission();
        teacher.setRole("TEACHER");
        teacher.setPermissions(List.of(
                "VIEW_OWN_PROFILE",
                "UPDATE_OWN_PROFILE",
                "VIEW_ALL_STUDENTS",
                "VIEW_STUDENT_MARKS",
                "CREATE_MARK",
                "UPDATE_MARK",
                "DELETE_MARK",
                "DELETE_STUDENT",
                "VIEW_OWN_MODULES",
                "UPDATE_OWN_MODULES",
                "ASSIGN_MODULE"
        ));

        rolePermissionRepository.save(student);
        rolePermissionRepository.save(teacher);
        log.info("Permissions seeded for STUDENT and TEACHER");
    }

}
