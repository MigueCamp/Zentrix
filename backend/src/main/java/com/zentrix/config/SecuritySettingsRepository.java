package com.zentrix.config;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SecuritySettingsRepository extends JpaRepository<SecuritySettings, Integer> {

    Optional<SecuritySettings> findByCompanyId(Integer companyId);
}
