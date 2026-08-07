package com.zentrix.auth;

import com.zentrix.auth.dto.LoginRequest;
import com.zentrix.auth.dto.LoginResponse;
import com.zentrix.common.security.AuthenticatedUser;
import com.zentrix.common.security.JwtService;
import com.zentrix.config.SecuritySettingsService;
import com.zentrix.user.User;
import com.zentrix.user.UserRepository;
import com.zentrix.user.UserRoleRepository;
import com.zentrix.user.UserStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecuritySettingsService securitySettingsService;

    public AuthService(UserRepository userRepository, UserRoleRepository userRoleRepository,
                        PasswordEncoder passwordEncoder, JwtService jwtService,
                        SecuritySettingsService securitySettingsService) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.securitySettingsService = securitySettingsService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndStatus(request.email(), UserStatus.ACTIVO)
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        Integer companyId = user.getCompany() != null ? user.getCompany().getId() : null;

        // Autoridad base según pertenencia a empresa; los roles personalizados
        // (ROL/USUARIO_ROL, ver docs/03_Modelo_de_Datos.md) se agregan encima.
        List<String> roles = new ArrayList<>();
        roles.add(companyId == null ? "SUPER_ADMIN" : "EMPRESA_ADMIN");
        userRoleRepository.findByUserId(user.getId())
                .forEach(userRole -> roles.add(userRole.getRole().getName()));

        AuthenticatedUser authenticatedUser = AuthenticatedUser.forUser(
                user.getId(), companyId, user.getEmail(), roles);

        // La expiración de sesión es configurable por empresa (módulo Configuración);
        // el Super Administrador (sin empresa) usa el valor por defecto global.
        String token = companyId == null
                ? jwtService.generateToken(authenticatedUser)
                : jwtService.generateToken(authenticatedUser, securitySettingsService.resolve(companyId).getSessionExpirationMinutes());

        return LoginResponse.bearer(token);
    }
}
