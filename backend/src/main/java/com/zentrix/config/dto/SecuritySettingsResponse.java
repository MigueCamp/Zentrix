package com.zentrix.config.dto;

import com.zentrix.config.SecuritySettings;

public record SecuritySettingsResponse(
        int passwordMinLength, boolean requireUppercase, boolean requireDigit,
        boolean requireSpecial, int sessionExpirationMinutes
) {
    public static SecuritySettingsResponse from(SecuritySettings settings) {
        return new SecuritySettingsResponse(
                settings.getPasswordMinLength(),
                settings.isRequireUppercase(),
                settings.isRequireDigit(),
                settings.isRequireSpecial(),
                settings.getSessionExpirationMinutes()
        );
    }
}
