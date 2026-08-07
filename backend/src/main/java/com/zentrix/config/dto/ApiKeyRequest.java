package com.zentrix.config.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApiKeyRequest(@NotBlank @Size(max = 150) String name) {
}
