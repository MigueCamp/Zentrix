package com.zentrix.config;

import com.zentrix.company.Company;
import com.zentrix.company.CompanyRepository;
import com.zentrix.user.AuditLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Reglas de negocio del módulo Configuración: la política de contraseñas por empresa
 * (docs/04, sección 8) se aplica según la config almacenada o los valores por defecto.
 */
@ExtendWith(MockitoExtension.class)
class SecuritySettingsServiceTest {

    @Mock SecuritySettingsRepository settingsRepository;
    @Mock CompanyRepository companyRepository;
    @Mock AuditLogService auditLogService;
    @InjectMocks SecuritySettingsService service;

    private static final int COMPANY_ID = 1;

    /** Sin config almacenada, resolve() cae a defaultsFor(company): min 8, mayúscula y dígito, sin especial. */
    private void withDefaults() {
        when(settingsRepository.findByCompanyId(COMPANY_ID)).thenReturn(Optional.empty());
        lenient().when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.of(Company.builder().id(COMPANY_ID).name("Acme").build()));
    }

    private SecuritySettings storedSettings(int min, boolean upper, boolean digit, boolean special) {
        return SecuritySettings.builder()
                .company(Company.builder().id(COMPANY_ID).build())
                .passwordMinLength(min).requireUppercase(upper).requireDigit(digit).requireSpecial(special)
                .sessionExpirationMinutes(30).build();
    }

    @Test
    @DisplayName("Contraseña más corta que el mínimo es rechazada")
    void tooShortRejected() {
        withDefaults();
        assertThatThrownBy(() -> service.validatePassword(COMPANY_ID, "Ab1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Falta de mayúscula es rechazada cuando la política la exige")
    void missingUppercaseRejected() {
        withDefaults();
        assertThatThrownBy(() -> service.validatePassword(COMPANY_ID, "abcdefg123"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Falta de dígito es rechazada cuando la política lo exige")
    void missingDigitRejected() {
        withDefaults();
        assertThatThrownBy(() -> service.validatePassword(COMPANY_ID, "AbcdefgHij"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Contraseña que cumple la política por defecto es aceptada")
    void validPasswordAccepted() {
        withDefaults();
        assertThatCode(() -> service.validatePassword(COMPANY_ID, "ValidPass123"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Falta de carácter especial es rechazada cuando la empresa lo exige")
    void missingSpecialRejectedWhenRequired() {
        when(settingsRepository.findByCompanyId(COMPANY_ID))
                .thenReturn(Optional.of(storedSettings(8, true, true, true)));
        assertThatThrownBy(() -> service.validatePassword(COMPANY_ID, "ValidPass123"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Con carácter especial se acepta cuando la empresa lo exige")
    void specialAcceptedWhenPresent() {
        when(settingsRepository.findByCompanyId(COMPANY_ID))
                .thenReturn(Optional.of(storedSettings(8, true, true, true)));
        assertThatCode(() -> service.validatePassword(COMPANY_ID, "ValidPass123!"))
                .doesNotThrowAnyException();
    }
}
