package com.zentrix.device;

import com.zentrix.common.tenant.TenantResolver;
import com.zentrix.device.dto.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Módulo "Gestión de Dispositivos" (docs/04_Especificación_de_Módulos.md, sección 3).
 * /devices/enroll y /devices/heartbeat los usa el agente Android, no la consola.
 */
@RestController
@RequestMapping("/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public DeviceCreatedResponse register(@RequestParam(required = false) Integer companyId,
                                           @Valid @RequestBody DeviceRequest request) {
        return deviceService.register(TenantResolver.resolve(companyId), request);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    public List<DeviceResponse> findAll(@RequestParam(required = false) Integer companyId) {
        return deviceService.findAll(TenantResolver.resolve(companyId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    public DeviceResponse findById(@RequestParam(required = false) Integer companyId, @PathVariable Integer id) {
        return deviceService.findById(TenantResolver.resolve(companyId), id);
    }

    @PutMapping("/{id}/group")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    public DeviceResponse assignGroup(@RequestParam(required = false) Integer companyId,
                                       @PathVariable Integer id, @RequestParam(required = false) Integer groupId) {
        return deviceService.assignGroup(TenantResolver.resolve(companyId), id, groupId);
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    public Page<DeviceEventResponse> history(@RequestParam(required = false) Integer companyId,
                                              @PathVariable Integer id,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return deviceService.history(TenantResolver.resolve(companyId), id, PageRequest.of(page, size));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestParam(required = false) Integer companyId, @PathVariable Integer id) {
        deviceService.delete(TenantResolver.resolve(companyId), id);
    }

    @PostMapping("/enroll")
    public EnrollResponse enroll(@Valid @RequestBody EnrollRequest request) {
        return deviceService.enroll(request);
    }

    @PostMapping("/heartbeat")
    @PreAuthorize("hasAuthority('DEVICE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void heartbeat(@Valid @RequestBody HeartbeatRequest request) {
        deviceService.heartbeat(request);
    }
}
