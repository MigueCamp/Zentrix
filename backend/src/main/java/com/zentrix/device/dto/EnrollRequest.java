package com.zentrix.device.dto;

import jakarta.validation.constraints.NotBlank;

public record EnrollRequest(@NotBlank String enrollmentToken, @NotBlank String imei, String serialNumber) {
}
