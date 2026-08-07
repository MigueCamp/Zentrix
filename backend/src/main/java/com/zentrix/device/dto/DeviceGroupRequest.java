package com.zentrix.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeviceGroupRequest(@NotBlank @Size(max = 150) String name) {
}
