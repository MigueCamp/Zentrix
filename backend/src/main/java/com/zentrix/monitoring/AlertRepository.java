package com.zentrix.monitoring;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Integer> {

    Page<Alert> findByDeviceCompanyIdOrderByCreatedAtDesc(Integer companyId, Pageable pageable);

    Page<Alert> findByDeviceCompanyIdAndAcknowledgedOrderByCreatedAtDesc(Integer companyId, boolean acknowledged, Pageable pageable);

    Optional<Alert> findByIdAndDeviceCompanyId(Integer id, Integer companyId);

    Optional<Alert> findFirstByDeviceIdAndTypeAndAcknowledgedFalse(Integer deviceId, String type);
}
