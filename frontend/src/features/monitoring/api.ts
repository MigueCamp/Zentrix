import { apiClient } from "@/services/api-client";
import { Alert, DeviceLocation, DeviceStatus } from "./types";

export async function fetchDeviceStatus(deviceId: number): Promise<DeviceStatus> {
  const { data } = await apiClient.get<DeviceStatus>(`/devices/${deviceId}/status`);
  return data;
}

export async function fetchDeviceLocation(deviceId: number): Promise<DeviceLocation> {
  const { data } = await apiClient.get<DeviceLocation>(`/devices/${deviceId}/location`);
  return data;
}

export async function fetchAlerts(acknowledged?: boolean): Promise<{ content: Alert[] }> {
  const { data } = await apiClient.get<{ content: Alert[] }>("/alerts", {
    params: acknowledged === undefined ? undefined : { acknowledged },
  });
  return data;
}

export async function acknowledgeAlert(id: number): Promise<Alert> {
  const { data } = await apiClient.put<Alert>(`/alerts/${id}/acknowledge`);
  return data;
}
