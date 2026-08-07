package com.zentrix.application;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceApplicationRepository extends JpaRepository<DeviceApplication, Integer> {

    Optional<DeviceApplication> findByDeviceIdAndApplicationId(Integer deviceId, Integer applicationId);

    List<DeviceApplication> findByApplicationId(Integer applicationId);
}
