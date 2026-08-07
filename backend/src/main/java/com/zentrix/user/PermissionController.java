package com.zentrix.user;

import com.zentrix.user.dto.PermissionResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Catálogo de permisos de la plataforma (fijo, ver docs/05_Seguridad_y_Cumplimiento.md).
 */
@RestController
public class PermissionController {

    private final PermissionRepository permissionRepository;

    public PermissionController(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @GetMapping("/permissions")
    public List<PermissionResponse> findAll() {
        return permissionRepository.findAll().stream().map(PermissionResponse::from).toList();
    }
}
