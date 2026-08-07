package com.zentrix.user.dto;

import com.zentrix.user.Permission;

public record PermissionResponse(Integer id, String code, String description) {

    public static PermissionResponse from(Permission permission) {
        return new PermissionResponse(permission.getId(), permission.getCode(), permission.getDescription());
    }
}
