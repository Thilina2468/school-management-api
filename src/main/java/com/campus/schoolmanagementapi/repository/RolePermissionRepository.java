package com.campus.schoolmanagementapi.repository;

import com.campus.schoolmanagementapi.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    RolePermission findByRole(String role);

}
