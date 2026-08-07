import { apiClient } from "@/services/api-client";
import { DeviceGroup, DeviceGroupInput } from "./types";

export async function fetchDeviceGroups(): Promise<DeviceGroup[]> {
  const { data } = await apiClient.get<DeviceGroup[]>("/device-groups");
  return data;
}

export async function createDeviceGroup(input: DeviceGroupInput): Promise<DeviceGroup> {
  const { data } = await apiClient.post<DeviceGroup>("/device-groups", input);
  return data;
}
