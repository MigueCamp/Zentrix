package com.zentrix.common.tenant;

/**
 * Holds the EmpresaId (tenant) of the currently authenticated request.
 * Populated by JwtAuthenticationFilter and cleared at the end of each request.
 */
public final class TenantContext {

    private static final ThreadLocal<Integer> CURRENT_COMPANY_ID = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setCurrentCompanyId(Integer companyId) {
        CURRENT_COMPANY_ID.set(companyId);
    }

    public static Integer getCurrentCompanyId() {
        return CURRENT_COMPANY_ID.get();
    }

    public static void clear() {
        CURRENT_COMPANY_ID.remove();
    }
}
