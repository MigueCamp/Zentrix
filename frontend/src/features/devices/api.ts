import { apiClient } from "@/services/api-client";
import { Device, DeviceCreated, DeviceInput } from "./types";

export async function fetchDevices(): Promise<Device[]> {
  const { data } = await apiClient.get<Device[]>("/devices");
  return data;
}

export async function registerDevice(input: DeviceInput): Promise<DeviceCreated> {
  const { data } = await apiClient.post<DeviceCreated>("/devices", input);
  return data;
}

export async function assignDeviceGroup(deviceId: number, groupId: number | null): Promise<Device> {
  const { data } = await apiClient.put<Device>(`/devices/${deviceId}/group`, null, {
    params: groupId != null ? { groupId } : undefined,
  });
  return data;
}
