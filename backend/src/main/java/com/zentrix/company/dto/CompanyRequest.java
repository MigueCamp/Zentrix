package com.zentrix.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 50) String taxId
) {
}
