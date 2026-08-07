package com.zentrix.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {

    List<RolePermission> findByRoleId(Integer roleId);

    void deleteByRoleId(Integer roleId);
}
