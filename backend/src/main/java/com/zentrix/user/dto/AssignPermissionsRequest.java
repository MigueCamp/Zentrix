package com.zentrix.user.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AssignPermissionsRequest(@NotNull List<Integer> permissionIds) {
}
