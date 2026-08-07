package com.zentrix.device;

import com.zentrix.common.ResourceNotFoundException;
import com.zentrix.company.Company;
import com.zentrix.company.CompanyRepository;
import com.zentrix.device.dto.DeviceGroupRequest;
import com.zentrix.device.dto.DeviceGroupResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DeviceGroupService {

    private final DeviceGroupRepository deviceGroupRepository;
    private final CompanyRepository companyRepository;

    public DeviceGroupService(DeviceGroupRepository deviceGroupRepository, CompanyRepository companyRepository) {
        this.deviceGroupRepository = deviceGroupRepository;
        this.companyRepository = companyRepository;
    }

    public DeviceGroupResponse create(Integer companyId, DeviceGroupRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada: " + companyId));

        DeviceGroup group = DeviceGroup.builder().company(company).name(request.name()).build();
        return DeviceGroupResponse.from(deviceGroupRepository.save(group));
    }

    @Transactional(readOnly = true)
    public List<DeviceGroupResponse> findAll(Integer companyId) {
        return deviceGroupRepository.findByCompanyId(companyId).stream().map(DeviceGroupResponse::from).toList();
    }
}
