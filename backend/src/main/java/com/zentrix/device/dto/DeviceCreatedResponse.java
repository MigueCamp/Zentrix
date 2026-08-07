package com.zentrix.device.dto;

import com.zentrix.device.Device;

/**
 * Se devuelve una única vez al pre-registrar un dispositivo: incluye el
 * TokenEnrollment que el agente Android necesita para completar el enroll.
 */
public record DeviceCreatedResponse(Integer id, String imei, String enrollmentToken) {

    public static DeviceCreatedResponse from(Device device) {
        return new DeviceCreatedResponse(device.getId(), device.getImei(), device.getEnrollmentToken());
    }
}
