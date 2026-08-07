package com.zentrix.report.dto;

import com.zentrix.device.DeviceEvent;

import java.time.LocalDateTime;

public record EventReportRow(Long id, Integer deviceId, String deviceImei, String type, String valueJson, LocalDateTime eventDate) {

    public static EventReportRow from(DeviceEvent event) {
        return new EventReportRow(
                event.getId(),
                event.getDevice().getId(),
                event.getDevice().getImei(),
                event.getType(),
                event.getValueJson(),
                event.getEventDate()
        );
    }
}
