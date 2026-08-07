package com.zentrix.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeviceRequest(
        @NotBlank @Size(max = 50) String imei,
        @Size(max = 100) String serialNumber,
        @Size(max = 100) String model,
        @Size(max = 20) String androidVersion
) {
}
