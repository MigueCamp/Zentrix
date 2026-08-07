package com.zentrix.config;

import com.zentrix.common.ResourceNotFoundException;
import com.zentrix.company.Company;
import com.zentrix.company.CompanyRepository;
import com.zentrix.config.dto.ApiKeyCreatedResponse;
import com.zentrix.config.dto.ApiKeyRequest;
import com.zentrix.config.dto.ApiKeyResponse;
import com.zentrix.user.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ApiKeyService {

    private static final String KEY_PREFIX = "zx_";
    private final SecureRandom random = new SecureRandom();

    private final ApiKeyRepository apiKeyRepository;
    private final CompanyRepository companyRepository;
    private final AuditLogService auditLogService;

    public ApiKeyService(ApiKeyRepository apiKeyRepository, CompanyRepository companyRepository,
                          AuditLogService auditLogService) {
        this.apiKeyRepository = apiKeyRepository;
        this.companyRepository = companyRepository;
        this.auditLogService = auditLogService;
    }

    public ApiKeyCreatedResponse create(Integer companyId, ApiKeyRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada: " + companyId));

        byte[] secret = new byte[32];
        random.nextBytes(secret);
        String plainKey = KEY_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(secret);

        ApiKey apiKey = ApiKey.builder()
                .company(company)
                .name(request.name())
                .prefix(plainKey.substring(0, 12))
                .keyHash(sha256Hex(plainKey))
                .active(true)
                .build();
        apiKey = apiKeyRepository.save(apiKey);

        auditLogService.record("CREAR_API_KEY", "{\"apiKeyId\":" + apiKey.getId() + ",\"nombre\":\"" + request.name() + "\"}");
        return ApiKeyCreatedResponse.of(apiKey, plainKey);
    }

    @Transactional(readOnly = true)
    public List<ApiKeyResponse> findAll(Integer companyId) {
        return apiKeyRepository.findByCompanyIdOrderByCreatedAtDesc(companyId).stream().map(ApiKeyResponse::from).toList();
    }

    public void revoke(Integer companyId, Integer id) {
        ApiKey apiKey = apiKeyRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("API key no encontrada: " + id));
        apiKey.setActive(false);
        apiKey.setRevokedAt(LocalDateTime.now());
        auditLogService.record("REVOCAR_API_KEY", "{\"apiKeyId\":" + id + "}");
    }

    /** Resuelve la empresa dueña de una API key activa (usado por ApiKeyAuthenticationFilter). */
    @Transactional(readOnly = true)
    public Optional<Integer> resolveCompanyId(String plainKey) {
        return apiKeyRepository.findByKeyHashAndActiveTrue(sha256Hex(plainKey))
                .map(apiKey -> apiKey.getCompany().getId());
    }

    private String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular el hash de la API key", e);
        }
    }
}
