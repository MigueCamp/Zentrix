package com.zentrix.user.dto;

import com.zentrix.user.AuditLog;

import java.time.LocalDateTime;

public record AuditLogResponse(Long id, String userEmail, String action, String detailJson, LocalDateTime actionDate) {

    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getUser().getEmail(),
                log.getAction(),
                log.getDetailJson(),
                log.getActionDate()
        );
    }
}
