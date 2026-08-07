package com.zentrix.device;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceGroupRepository extends JpaRepository<DeviceGroup, Integer> {

    List<DeviceGroup> findByCompanyId(Integer companyId);

    Optional<DeviceGroup> findByIdAndCompanyId(Integer id, Integer companyId);
}
