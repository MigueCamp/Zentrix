package com.zentrix.common.security;

import com.zentrix.common.tenant.TenantContext;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Resolves the AuthenticatedUser from the Bearer token and seeds both the
 * Spring Security context and the TenantContext used for multi-tenant filtering.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String token = resolveToken(request);
            if (token != null) {
                try {
                    AuthenticatedUser user = jwtService.parseToken(token);
                    List<GrantedAuthority> authorities = user.roles() == null
                            ? List.of()
                            : user.roles().stream().<GrantedAuthority>map(SimpleGrantedAuthority::new).toList();

                    var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    TenantContext.setCurrentCompanyId(user.companyId());
                } catch (JwtException ignored) {
                    // Invalid/expired token: request continues unauthenticated,
                    // Spring Security denies access to protected endpoints below.
                }
            }
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
