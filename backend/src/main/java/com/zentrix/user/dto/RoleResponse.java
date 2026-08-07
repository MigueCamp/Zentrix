package com.zentrix.user.dto;

import com.zentrix.user.Role;

import java.util.List;

public record RoleResponse(Integer id, String name, List<PermissionResponse> permissions) {

    public static RoleResponse from(Role role, List<PermissionResponse> permissions) {
        return new RoleResponse(role.getId(), role.getName(), permissions);
    }
}
