package com.zentrix.command;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceCommandRepository extends JpaRepository<DeviceCommand, Long> {

    List<DeviceCommand> findByDeviceIdAndStatus(Integer deviceId, CommandStatus status);

    Page<DeviceCommand> findByDeviceIdOrderByCreatedAtDesc(Integer deviceId, Pageable pageable);
}
