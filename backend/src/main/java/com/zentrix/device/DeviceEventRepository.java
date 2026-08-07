package com.zentrix.device;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface DeviceEventRepository extends JpaRepository<DeviceEvent, Long> {

    Page<DeviceEvent> findByDeviceIdOrderByEventDateDesc(Integer deviceId, Pageable pageable);

    Optional<DeviceEvent> findFirstByDeviceIdAndTypeOrderByEventDateDesc(Integer deviceId, String type);

    Page<DeviceEvent> findByDeviceCompanyIdAndEventDateBetweenOrderByEventDateDesc(
            Integer companyId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<DeviceEvent> findByDeviceCompanyIdAndTypeAndEventDateBetweenOrderByEventDateDesc(
            Integer companyId, String type, LocalDateTime from, LocalDateTime to, Pageable pageable);
}
