package com.zentrix.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Integer> {

    List<Permission> findByIdIn(List<Integer> ids);
}
