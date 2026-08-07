package com.zentrix.config;

import com.zentrix.common.ResourceNotFoundException;
import com.zentrix.company.Company;
import com.zentrix.company.CompanyRepository;
import com.zentrix.config.dto.SecuritySettingsRequest;
import com.zentrix.config.dto.SecuritySettingsResponse;
import com.zentrix.user.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SecuritySettingsService {

    private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    private final SecuritySettingsRepository settingsRepository;
    private final CompanyRepository companyRepository;
    private final AuditLogService auditLogService;

    public SecuritySettingsService(SecuritySettingsRepository settingsRepository,
                                    CompanyRepository companyRepository, AuditLogService auditLogService) {
        this.settingsRepository = settingsRepository;
        this.companyRepository = companyRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public SecuritySettingsResponse get(Integer companyId) {
        return SecuritySettingsResponse.from(resolve(companyId));
    }

    /** Devuelve la configuración de la empresa o los valores por defecto (sin persistir). */
    @Transactional(readOnly = true)
    public SecuritySettings resolve(Integer companyId) {
        return settingsRepository.findByCompanyId(companyId)
                .orElseGet(() -> SecuritySettings.defaultsFor(referenceCompany(companyId)));
    }

    public SecuritySettingsResponse update(Integer companyId, SecuritySettingsRequest request) {
        SecuritySettings settings = settingsRepository.findByCompanyId(companyId)
                .orElseGet(() -> SecuritySettings.defaultsFor(referenceCompany(companyId)));
        settings.setPasswordMinLength(request.passwordMinLength());
        settings.setRequireUppercase(request.requireUppercase());
        settings.setRequireDigit(request.requireDigit());
        settings.setRequireSpecial(request.requireSpecial());
        settings.setSessionExpirationMinutes(request.sessionExpirationMinutes());
        settings = settingsRepository.save(settings);

        auditLogService.record("ACTUALIZAR_SEGURIDAD",
                "{\"passwordMinLength\":" + settings.getPasswordMinLength()
                        + ",\"sessionExpirationMinutes\":" + settings.getSessionExpirationMinutes() + "}");
        return SecuritySettingsResponse.from(settings);
    }

    /** Valida la contraseña contra la política de la empresa; lanza 400 si no cumple. */
    @Transactional(readOnly = true)
    public void validatePassword(Integer companyId, String password) {
        SecuritySettings settings = resolve(companyId);
        if (password.length() < settings.getPasswordMinLength()) {
            throw new IllegalArgumentException("La contraseña debe tener al menos " + settings.getPasswordMinLength() + " caracteres");
        }
        if (settings.isRequireUppercase() && password.chars().noneMatch(Character::isUpperCase)) {
            throw new IllegalArgumentException("La contraseña debe incluir al menos una mayúscula");
        }
        if (settings.isRequireDigit() && password.chars().noneMatch(Character::isDigit)) {
            throw new IllegalArgumentException("La contraseña debe incluir al menos un dígito");
        }
        if (settings.isRequireSpecial() && password.chars().noneMatch(c -> SPECIAL_CHARS.indexOf(c) >= 0)) {
            throw new IllegalArgumentException("La contraseña debe incluir al menos un carácter especial");
        }
    }

    private Company referenceCompany(Integer companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada: " + companyId));
    }
}
