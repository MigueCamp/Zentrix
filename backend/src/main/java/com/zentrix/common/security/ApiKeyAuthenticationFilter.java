package com.zentrix.common.security;

import com.zentrix.common.tenant.TenantContext;
import com.zentrix.config.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Autentica integraciones externas por el header X-API-Key. La clave se resuelve a la
 * empresa dueña (ApiKeyService) y se le otorga el rol API_CLIENT, acotado a endpoints de
 * solo lectura (módulo Reportes). El aislamiento multi-tenant se hereda vía TenantContext.
 */
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";

    private final ApiKeyService apiKeyService;

    public ApiKeyAuthenticationFilter(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String apiKey = request.getHeader(API_KEY_HEADER);
        if (StringUtils.hasText(apiKey) && SecurityContextHolder.getContext().getAuthentication() == null) {
            apiKeyService.resolveCompanyId(apiKey).ifPresent(companyId -> {
                AuthenticatedUser principal = new AuthenticatedUser(null, null, companyId, "api-key", List.of("API_CLIENT"));
                var authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, List.of(new SimpleGrantedAuthority("API_CLIENT")));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                TenantContext.setCurrentCompanyId(companyId);
            });
        }
        chain.doFilter(request, response);
    }
}
