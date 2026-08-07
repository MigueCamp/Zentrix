package com.zentrix.application.dto;

import com.zentrix.application.Application;

import java.time.LocalDateTime;

public record ApplicationResponse(
        Integer id, String name, String packageName, String currentVersion,
        String fileUrl, LocalDateTime uploadedAt
) {
    public static ApplicationResponse from(Application application) {
        return new ApplicationResponse(
                application.getId(),
                application.getName(),
                application.getPackageName(),
                application.getCurrentVersion(),
                "/applications/" + application.getId() + "/apk",
                application.getUploadedAt()
        );
    }
}
