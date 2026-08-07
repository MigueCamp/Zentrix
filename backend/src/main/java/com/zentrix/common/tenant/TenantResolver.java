package com.zentrix.common.tenant;

import org.springframework.security.access.AccessDeniedException;

/**
 * Resuelve el EmpresaId efectivo de una operación: un EMPRESA_ADMIN siempre
 * opera sobre su propia empresa (TenantContext); un SUPER_ADMIN (sin empresa
 * propia) debe indicar explícitamente sobre qué empresa opera.
 */
public final class TenantResolver {

    private TenantResolver() {
    }

    public static Integer resolve(Integer requestedCompanyId) {
        Integer ownCompanyId = TenantContext.getCurrentCompanyId();

        if (ownCompanyId != null) {
            if (requestedCompanyId != null && !requestedCompanyId.equals(ownCompanyId)) {
                throw new AccessDeniedException("No puede operar sobre otra empresa");
            }
            return ownCompanyId;
        }

        if (requestedCompanyId == null) {
            throw new IllegalArgumentException("Debe especificar companyId");
        }
        return requestedCompanyId;
    }
}
