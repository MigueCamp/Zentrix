package com.zentrix.policy;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PolicyRepository extends JpaRepository<Policy, Integer> {

    List<Policy> findByCompanyId(Integer companyId);

    Optional<Policy> findByIdAndCompanyId(Integer id, Integer companyId);
}
