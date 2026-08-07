package com.zentrix.user.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AssignRolesRequest(@NotNull List<Integer> roleIds) {
}
