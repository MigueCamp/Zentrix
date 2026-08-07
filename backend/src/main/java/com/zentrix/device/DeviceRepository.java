package com.zentrix.device;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Integer> {

    boolean existsByImei(String imei);

    Optional<Device> findByEnrollmentToken(String enrollmentToken);

    List<Device> findByCompanyIdAndStatusNot(Integer companyId, DeviceStatus excludedStatus);

    Optional<Device> findByIdAndCompanyId(Integer id, Integer companyId);

    List<Device> findByGroupId(Integer groupId);

    List<Device> findByStatusAndLastSeenAtBefore(DeviceStatus status, LocalDateTime threshold);
}
