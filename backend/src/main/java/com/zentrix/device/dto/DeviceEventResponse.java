package com.zentrix.device.dto;

import com.zentrix.device.DeviceEvent;

import java.time.LocalDateTime;

public record DeviceEventResponse(Long id, String type, String valueJson, LocalDateTime eventDate) {

    public static DeviceEventResponse from(DeviceEvent event) {
        return new DeviceEventResponse(event.getId(), event.getType(), event.getValueJson(), event.getEventDate());
    }
}
