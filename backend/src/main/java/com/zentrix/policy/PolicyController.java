package com.zentrix.policy;

import com.zentrix.common.tenant.TenantResolver;
import com.zentrix.policy.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Módulo "Perfiles y Políticas" (docs/04_Especificación_de_Módulos.md, sección 4).
 */
@RestController
@RequestMapping("/policies")
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'EMPRESA_ADMIN')")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PolicyResponse create(@RequestParam(required = false) Integer companyId,
                                  @Valid @RequestBody PolicyRequest request) {
        return policyService.create(TenantResolver.resolve(companyId), request);
    }

    @GetMapping
    public List<PolicyResponse> findAll(@RequestParam(required = false) Integer companyId) {
        return policyService.findAll(TenantResolver.resolve(companyId));
    }

    @GetMapping("/{id}")
    public PolicyResponse findById(@RequestParam(required = false) Integer companyId, @PathVariable Integer id) {
        return policyService.findById(TenantResolver.resolve(companyId), id);
    }

    @PutMapping("/{id}")
    public PolicyResponse update(@RequestParam(required = false) Integer companyId, @PathVariable Integer id,
                                  @Valid @RequestBody PolicyRequest request) {
        return policyService.update(TenantResolver.resolve(companyId), id, request);
    }

    @PostMapping("/{id}/assign")
    public PolicyAssignmentResponse assign(@RequestParam(required = false) Integer companyId, @PathVariable Integer id,
                                            @RequestBody PolicyAssignRequest request) {
        return policyService.assign(TenantResolver.resolve(companyId), id, request);
    }

    @GetMapping("/assignments")
    public List<PolicyAssignmentResponse> findAssignments(@RequestParam(required = false) Integer companyId) {
        return policyService.findAssignments(TenantResolver.resolve(companyId));
    }
}
