package com.zentrix.config;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Integer> {

    List<ApiKey> findByCompanyIdOrderByCreatedAtDesc(Integer companyId);

    Optional<ApiKey> findByIdAndCompanyId(Integer id, Integer companyId);

    Optional<ApiKey> findByKeyHashAndActiveTrue(String keyHash);
}
