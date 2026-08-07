package com.zentrix.monitoring;

import com.zentrix.common.tenant.TenantResolver;
import com.zentrix.monitoring.dto.AlertResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Módulo "Monitoreo" — alertas (docs/04, sección 6).
 */
@RestController
@RequestMapping("/alerts")
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'EMPRESA_ADMIN')")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public Page<AlertResponse> findAll(@RequestParam(required = false) Integer companyId,
                                        @RequestParam(required = false) Boolean acknowledged,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return alertService.findForCompany(TenantResolver.resolve(companyId), acknowledged, PageRequest.of(page, size));
    }

    @PutMapping("/{id}/acknowledge")
    public AlertResponse acknowledge(@RequestParam(required = false) Integer companyId, @PathVariable Integer id) {
        return alertService.acknowledge(TenantResolver.resolve(companyId), id);
    }
}
