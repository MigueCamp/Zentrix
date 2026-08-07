package com.zentrix.user;

import com.zentrix.common.DuplicateResourceException;
import com.zentrix.common.ResourceNotFoundException;
import com.zentrix.company.Company;
import com.zentrix.company.CompanyRepository;
import com.zentrix.user.dto.AssignPermissionsRequest;
import com.zentrix.user.dto.PermissionResponse;
import com.zentrix.user.dto.RoleRequest;
import com.zentrix.user.dto.RoleResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final CompanyRepository companyRepository;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository,
                        RolePermissionRepository rolePermissionRepository, CompanyRepository companyRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.companyRepository = companyRepository;
    }

    public RoleResponse create(Integer companyId, RoleRequest request) {
        if (roleRepository.existsByCompanyIdAndName(companyId, request.name())) {
            throw new DuplicateResourceException("Ya existe un rol llamado " + request.name());
        }
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada: " + companyId));

        Role role = Role.builder().company(company).name(request.name()).build();
        return toResponse(roleRepository.save(role));
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> findAll(Integer companyId) {
        return roleRepository.findByCompanyId(companyId).stream().map(this::toResponse).toList();
    }

    public RoleResponse assignPermissions(Integer companyId, Integer roleId, AssignPermissionsRequest request) {
        Role role = roleRepository.findByIdAndCompanyId(roleId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + roleId));

        List<Permission> permissions = permissionRepository.findByIdIn(request.permissionIds());
        rolePermissionRepository.deleteByRoleId(roleId);
        permissions.forEach(permission ->
                rolePermissionRepository.save(new RolePermission(role, permission)));

        return toResponse(role);
    }

    private RoleResponse toResponse(Role role) {
        List<PermissionResponse> permissions = rolePermissionRepository.findByRoleId(role.getId()).stream()
                .map(rolePermission -> PermissionResponse.from(rolePermission.getPermission()))
                .toList();
        return RoleResponse.from(role, permissions);
    }
}
