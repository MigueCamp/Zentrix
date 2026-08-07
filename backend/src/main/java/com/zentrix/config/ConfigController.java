package com.zentrix.config;

import com.zentrix.common.tenant.TenantResolver;
import com.zentrix.config.dto.*;
import com.zentrix.user.AuditLogService;
import com.zentrix.user.dto.AuditLogResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Módulo "Configuración" (docs/04_Especificación_de_Módulos.md, sección 8):
 * seguridad, API keys y acceso centralizado a logs de auditoría.
 */
@RestController
@RequestMapping("/settings")
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'EMPRESA_ADMIN')")
public class ConfigController {

    private final SecuritySettingsService securitySettingsService;
    private final ApiKeyService apiKeyService;
    private final AuditLogService auditLogService;

    public ConfigController(SecuritySettingsService securitySettingsService, ApiKeyService apiKeyService,
                             AuditLogService auditLogService) {
        this.securitySettingsService = securitySettingsService;
        this.apiKeyService = apiKeyService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/security")
    public SecuritySettingsResponse getSecurity(@RequestParam(required = false) Integer companyId) {
        return securitySettingsService.get(TenantResolver.resolve(companyId));
    }

    @PutMapping("/security")
    public SecuritySettingsResponse updateSecurity(@RequestParam(required = false) Integer companyId,
                                                    @Valid @RequestBody SecuritySettingsRequest request) {
        return securitySettingsService.update(TenantResolver.resolve(companyId), request);
    }

    @GetMapping("/api-keys")
    public List<ApiKeyResponse> listApiKeys(@RequestParam(required = false) Integer companyId) {
        return apiKeyService.findAll(TenantResolver.resolve(companyId));
    }

    @PostMapping("/api-keys")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiKeyCreatedResponse createApiKey(@RequestParam(required = false) Integer companyId,
                                               @Valid @RequestBody ApiKeyRequest request) {
        return apiKeyService.create(TenantResolver.resolve(companyId), request);
    }

    @DeleteMapping("/api-keys/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeApiKey(@RequestParam(required = false) Integer companyId, @PathVariable Integer id) {
        apiKeyService.revoke(TenantResolver.resolve(companyId), id);
    }

    @GetMapping("/logs")
    public Page<AuditLogResponse> logs(@RequestParam(required = false) Integer companyId,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return auditLogService.findForCurrentTenant(TenantResolver.resolve(companyId), PageRequest.of(page, size));
    }
}
