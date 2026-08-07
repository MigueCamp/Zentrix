package com.zentrix.device;

import com.zentrix.common.tenant.TenantResolver;
import com.zentrix.device.dto.DeviceGroupRequest;
import com.zentrix.device.dto.DeviceGroupResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Módulo "Dispositivos → Agrupar Equipos" (docs/04_Especificación_de_Módulos.md, sección 3).
 */
@RestController
@RequestMapping("/device-groups")
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'EMPRESA_ADMIN')")
public class DeviceGroupController {

    private final DeviceGroupService deviceGroupService;

    public DeviceGroupController(DeviceGroupService deviceGroupService) {
        this.deviceGroupService = deviceGroupService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeviceGroupResponse create(@RequestParam(required = false) Integer companyId,
                                       @Valid @RequestBody DeviceGroupRequest request) {
        return deviceGroupService.create(TenantResolver.resolve(companyId), request);
    }

    @GetMapping
    public List<DeviceGroupResponse> findAll(@RequestParam(required = false) Integer companyId) {
        return deviceGroupService.findAll(TenantResolver.resolve(companyId));
    }
}
