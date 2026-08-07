package com.zentrix.device.dto;

import com.zentrix.device.DeviceGroup;

public record DeviceGroupResponse(Integer id, String name) {

    public static DeviceGroupResponse from(DeviceGroup group) {
        return new DeviceGroupResponse(group.getId(), group.getName());
    }
}
