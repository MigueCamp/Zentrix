package com.zentrix.config.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SecuritySettingsRequest(
        @Min(6) @Max(128) int passwordMinLength,
        boolean requireUppercase,
        boolean requireDigit,
        boolean requireSpecial,
        @Min(5) @Max(1440) int sessionExpirationMinutes
) {
}
