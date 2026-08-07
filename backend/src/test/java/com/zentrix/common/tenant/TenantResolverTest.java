package com.zentrix.common.tenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Suite de aislamiento multi-tenant (docs/07, sección 3): el corazón del control de acceso
 * entre empresas vive en TenantResolver. Verifica que un EMPRESA_ADMIN nunca opere sobre otra
 * empresa y que un SUPER_ADMIN deba declarar explícitamente sobre cuál opera.
 */
class TenantResolverTest {

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("EMPRESA_ADMIN sin companyId solicitado opera sobre su propia empresa")
    void companyAdminDefaultsToOwnCompany() {
        TenantContext.setCurrentCompanyId(7);
        assertThat(TenantResolver.resolve(null)).isEqualTo(7);
    }

    @Test
    @DisplayName("EMPRESA_ADMIN con companyId igual al propio opera sobre su empresa")
    void companyAdminMatchingCompanyIsAllowed() {
        TenantContext.setCurrentCompanyId(7);
        assertThat(TenantResolver.resolve(7)).isEqualTo(7);
    }

    @Test
    @DisplayName("EMPRESA_ADMIN NO puede operar sobre otra empresa aunque conozca su ID")
    void companyAdminCannotCrossTenant() {
        TenantContext.setCurrentCompanyId(7);
        assertThatThrownBy(() -> TenantResolver.resolve(9))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("SUPER_ADMIN sin companyId explícito recibe error (debe declarar el tenant)")
    void superAdminMustSpecifyCompany() {
        TenantContext.setCurrentCompanyId(null);
        assertThatThrownBy(() -> TenantResolver.resolve(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("SUPER_ADMIN con companyId explícito opera sobre la empresa indicada")
    void superAdminUsesRequestedCompany() {
        TenantContext.setCurrentCompanyId(null);
        assertThat(TenantResolver.resolve(42)).isEqualTo(42);
    }
}
