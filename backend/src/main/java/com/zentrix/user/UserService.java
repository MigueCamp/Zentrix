package com.zentrix.user;

import com.zentrix.common.DuplicateResourceException;
import com.zentrix.common.ResourceNotFoundException;
import com.zentrix.company.Company;
import com.zentrix.company.CompanyRepository;
import com.zentrix.config.SecuritySettingsService;
import com.zentrix.user.dto.AssignRolesRequest;
import com.zentrix.user.dto.UserRequest;
import com.zentrix.user.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final SecuritySettingsService securitySettingsService;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                        UserRoleRepository userRoleRepository, CompanyRepository companyRepository,
                        PasswordEncoder passwordEncoder, AuditLogService auditLogService,
                        SecuritySettingsService securitySettingsService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
        this.securitySettingsService = securitySettingsService;
    }

    public UserResponse create(Integer companyId, UserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Ya existe un usuario con el correo " + request.email());
        }
        securitySettingsService.validatePassword(companyId, request.password());
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada: " + companyId));

        User user = User.builder()
                .company(company)
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .status(UserStatus.ACTIVO)
                .build();
        user = userRepository.save(user);

        auditLogService.record("CREAR_USUARIO", "{\"email\":\"" + request.email() + "\"}");
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll(Integer companyId) {
        return userRepository.findByCompanyId(companyId).stream().map(this::toResponse).toList();
    }

    public UserResponse assignRoles(Integer companyId, Integer userId, AssignRolesRequest request) {
        User user = userRepository.findByIdAndCompanyId(userId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + userId));

        List<Role> roles = request.roleIds().stream()
                .map(roleId -> roleRepository.findByIdAndCompanyId(roleId, companyId)
                        .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + roleId)))
                .toList();

        userRoleRepository.deleteAll(userRoleRepository.findByUserId(userId));
        roles.forEach(role -> userRoleRepository.save(new UserRole(user, role)));

        auditLogService.record("ASIGNAR_ROLES", "{\"userId\":" + userId + ",\"roleIds\":" + request.roleIds() + "}");
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        List<String> roles = userRoleRepository.findByUserId(user.getId()).stream()
                .map(userRole -> userRole.getRole().getName())
                .toList();
        return UserResponse.from(user, roles);
    }
}
