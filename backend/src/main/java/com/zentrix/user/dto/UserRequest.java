package com.zentrix.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Email @Size(max = 200) String email,
        @NotBlank @Size(min = 8, max = 100) String password
) {
}
