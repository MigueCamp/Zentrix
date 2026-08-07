package com.zentrix.policy.dto;

import com.zentrix.policy.PolicyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PolicyRequest(
        @NotBlank @Size(max = 150) String name,
        @NotNull PolicyType type,
        @NotBlank String configurationJson
) {
}
