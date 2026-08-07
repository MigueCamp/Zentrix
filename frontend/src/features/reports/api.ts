import { apiClient } from "@/services/api-client";
import { Device } from "@/features/devices/types";
import { DeviceEventRow, EventsQuery } from "./types";

export async function fetchInventoryReport(): Promise<Device[]> {
  const { data } = await apiClient.get<Device[]>("/reports/inventory");
  return data;
}

export async function fetchEventsReport(query: EventsQuery): Promise<{ content: DeviceEventRow[]; totalElements: number }> {
  const { data } = await apiClient.get<{ content: DeviceEventRow[]; totalElements: number }>("/reports/events", {
    params: query,
  });
  return data;
}

export async function downloadInventoryExport(format: "pdf" | "xlsx"): Promise<void> {
  const { data } = await apiClient.get<Blob>("/reports/inventory/export", {
    params: { format },
    responseType: "blob",
  });
  const url = URL.createObjectURL(data);
  const link = document.createElement("a");
  link.href = url;
  link.download = `inventario.${format}`;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}
