package com.zentrix.user;

import com.zentrix.common.tenant.TenantResolver;
import com.zentrix.user.dto.AssignRolesRequest;
import com.zentrix.user.dto.UserRequest;
import com.zentrix.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Módulo "Administración de Usuarios" (docs/04_Especificación_de_Módulos.md, sección 2).
 * companyId es opcional para EMPRESA_ADMIN (se usa su propia empresa) y
 * obligatorio para SUPER_ADMIN (no tiene empresa propia) — ver TenantResolver.
 */
@RestController
@RequestMapping("/users")
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'EMPRESA_ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@RequestParam(required = false) Integer companyId,
                                @Valid @RequestBody UserRequest request) {
        return userService.create(TenantResolver.resolve(companyId), request);
    }

    @GetMapping
    public List<UserResponse> findAll(@RequestParam(required = false) Integer companyId) {
        return userService.findAll(TenantResolver.resolve(companyId));
    }

    @PostMapping("/{id}/roles")
    public UserResponse assignRoles(@RequestParam(required = false) Integer companyId,
                                     @PathVariable Integer id, @Valid @RequestBody AssignRolesRequest request) {
        return userService.assignRoles(TenantResolver.resolve(companyId), id, request);
    }
}
