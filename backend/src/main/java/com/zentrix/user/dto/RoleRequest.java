package com.zentrix.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoleRequest(@NotBlank @Size(max = 150) String name) {
}
