package com.zentrix.config;

import com.zentrix.company.Company;
import com.zentrix.company.CompanyRepository;
import com.zentrix.config.dto.ApiKeyCreatedResponse;
import com.zentrix.config.dto.ApiKeyRequest;
import com.zentrix.user.AuditLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Módulo Configuración — API keys: la clave en claro se muestra una sola vez y solo se
 * guarda su hash SHA-256 (determinista para permitir la búsqueda en autenticación).
 */
@ExtendWith(MockitoExtension.class)
class ApiKeyServiceTest {

    @Mock ApiKeyRepository apiKeyRepository;
    @Mock CompanyRepository companyRepository;
    @Mock AuditLogService auditLogService;
    @InjectMocks ApiKeyService service;

    private static final int COMPANY_ID = 3;

    @Test
    @DisplayName("create genera una clave con prefijo zx_ y solo persiste el hash, no el valor en claro")
    void createStoresHashNotPlaintext() {
        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.of(Company.builder().id(COMPANY_ID).name("Acme").build()));
        when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(inv -> inv.getArgument(0));

        ApiKeyCreatedResponse created = service.create(COMPANY_ID, new ApiKeyRequest("integración-erp"));

        ArgumentCaptor<ApiKey> saved = ArgumentCaptor.forClass(ApiKey.class);
        org.mockito.Mockito.verify(apiKeyRepository).save(saved.capture());

        assertThat(created.apiKey()).startsWith("zx_");
        assertThat(created.prefix()).hasSize(12);
        // El hash almacenado nunca es el valor en claro y tiene la longitud de un SHA-256 hex.
        assertThat(saved.getValue().getKeyHash())
                .isNotEqualTo(created.apiKey())
                .hasSize(64);
    }

    @Test
    @DisplayName("resolveCompanyId hashea la clave en claro igual que en create y encuentra su empresa")
    void resolveCompanyIdMatchesStoredHash() {
        when(companyRepository.findById(COMPANY_ID))
                .thenReturn(Optional.of(Company.builder().id(COMPANY_ID).name("Acme").build()));
        when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<ApiKey> saved = ArgumentCaptor.forClass(ApiKey.class);
        ApiKeyCreatedResponse created = service.create(COMPANY_ID, new ApiKeyRequest("integración"));
        org.mockito.Mockito.verify(apiKeyRepository).save(saved.capture());
        String storedHash = saved.getValue().getKeyHash();

        // El filtro de autenticación busca por ese mismo hash: simulamos el hit.
        when(apiKeyRepository.findByKeyHashAndActiveTrue(storedHash))
                .thenReturn(Optional.of(ApiKey.builder()
                        .company(Company.builder().id(COMPANY_ID).build())
                        .active(true).build()));

        assertThat(service.resolveCompanyId(created.apiKey())).contains(COMPANY_ID);
    }

    @Test
    @DisplayName("una clave desconocida no resuelve ninguna empresa")
    void unknownKeyResolvesNothing() {
        when(apiKeyRepository.findByKeyHashAndActiveTrue(any())).thenReturn(Optional.empty());
        assertThat(service.resolveCompanyId("zx_desconocida")).isEmpty();
    }

    @Test
    @DisplayName("revoke marca la clave como inactiva y registra la fecha de revocación")
    void revokeDeactivatesKey() {
        ApiKey key = ApiKey.builder()
                .company(Company.builder().id(COMPANY_ID).build())
                .name("k").active(true).build();
        when(apiKeyRepository.findByIdAndCompanyId(10, COMPANY_ID)).thenReturn(Optional.of(key));

        service.revoke(COMPANY_ID, 10);

        assertThat(key.isActive()).isFalse();
        assertThat(key.getRevokedAt()).isNotNull();
    }
}
