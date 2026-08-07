package com.zentrix.device.dto;

/**
 * memoryUsedBytes/memoryTotalBytes y latitude/longitude son opcionales: dependen del
 * permiso de ubicación otorgado en el dispositivo (docs/04, sección 6).
 */
public record HeartbeatRequest(
        int batteryLevel, long storageFreeBytes,
        Long memoryUsedBytes, Long memoryTotalBytes,
        Double latitude, Double longitude
) {
}
