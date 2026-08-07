package com.zentrix.user;

import com.zentrix.common.tenant.TenantContext;
import com.zentrix.user.dto.AuditLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Módulo "Usuarios → Auditoría" (docs/04_Especificación_de_Módulos.md, sección 2).
 */
@RestController
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/audit-logs")
    public Page<AuditLogResponse> findAll(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        return auditLogService.findForCurrentTenant(TenantContext.getCurrentCompanyId(), PageRequest.of(page, size));
    }
}
