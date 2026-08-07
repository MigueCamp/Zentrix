package com.zentrix.monitoring.dto;

import java.time.LocalDateTime;

public record DeviceLocationResponse(Integer deviceId, Double latitude, Double longitude, LocalDateTime reportedAt) {
}
