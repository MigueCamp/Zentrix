package com.zentrix.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    List<Role> findByCompanyId(Integer companyId);

    Optional<Role> findByIdAndCompanyId(Integer id, Integer companyId);

    boolean existsByCompanyIdAndName(Integer companyId, String name);
}
