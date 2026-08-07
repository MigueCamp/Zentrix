package com.zentrix.monitoring;

import com.zentrix.common.tenant.TenantResolver;
import com.zentrix.monitoring.dto.DeviceLocationResponse;
import com.zentrix.monitoring.dto.DeviceStatusResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Módulo "Monitoreo" (docs/04_Especificación_de_Módulos.md, sección 6).
 */
@RestController
@RequestMapping("/devices")
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'EMPRESA_ADMIN')")
public class MonitoringController {

    private final MonitoringService monitoringService;

    public MonitoringController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @GetMapping("/{id}/status")
    public DeviceStatusResponse status(@RequestParam(required = false) Integer companyId, @PathVariable Integer id) {
        return monitoringService.status(TenantResolver.resolve(companyId), id);
    }

    @GetMapping("/{id}/location")
    public DeviceLocationResponse location(@RequestParam(required = false) Integer companyId, @PathVariable Integer id) {
        return monitoringService.location(TenantResolver.resolve(companyId), id);
    }
}
