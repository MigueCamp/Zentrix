package com.zentrix.common.security;

import java.util.List;

/**
 * Security principal resolved from the JWT on every request.
 * companyId is null only for the SUPER_ADMIN role (cross-tenant access).
 * userId is set for user-issued tokens, deviceId for device-issued tokens
 * (mutually exclusive) — ver docs/02_Arquitectura_del_Sistema.md, sección 4.2.
 */
public record AuthenticatedUser(
        Integer userId, Integer deviceId, Integer companyId, String subject, List<String> roles
) {
    public static AuthenticatedUser forUser(Integer userId, Integer companyId, String email, List<String> roles) {
        return new AuthenticatedUser(userId, null, companyId, email, roles);
    }

    public static AuthenticatedUser forDevice(Integer deviceId, Integer companyId, String imei) {
        return new AuthenticatedUser(null, deviceId, companyId, imei, List.of("DEVICE"));
    }
}
