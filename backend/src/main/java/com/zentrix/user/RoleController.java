package com.zentrix.user;

import com.zentrix.common.tenant.TenantResolver;
import com.zentrix.user.dto.AssignPermissionsRequest;
import com.zentrix.user.dto.RoleRequest;
import com.zentrix.user.dto.RoleResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Módulo "Usuarios → Asignar Roles / Permisos" (docs/04_Especificación_de_Módulos.md, sección 2).
 */
@RestController
@RequestMapping("/roles")
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'EMPRESA_ADMIN')")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse create(@RequestParam(required = false) Integer companyId,
                                @Valid @RequestBody RoleRequest request) {
        return roleService.create(TenantResolver.resolve(companyId), request);
    }

    @GetMapping
    public List<RoleResponse> findAll(@RequestParam(required = false) Integer companyId) {
        return roleService.findAll(TenantResolver.resolve(companyId));
    }

    @PutMapping("/{id}/permissions")
    public RoleResponse assignPermissions(@RequestParam(required = false) Integer companyId,
                                           @PathVariable Integer id,
                                           @Valid @RequestBody AssignPermissionsRequest request) {
        return roleService.assignPermissions(TenantResolver.resolve(companyId), id, request);
    }
}
