export interface DeviceStatus {
  deviceId: number;
  online: boolean;
  batteryLevel: number | null;
  storageFreeBytes: number | null;
  memoryUsedBytes: number | null;
  memoryTotalBytes: number | null;
  lastSeenAt: string | null;
}

export interface DeviceLocation {
  deviceId: number;
  latitude: number | null;
  longitude: number | null;
  reportedAt: string | null;
}

export interface Alert {
  id: number;
  deviceId: number;
  deviceImei: string;
  type: string;
  severity: "BAJA" | "MEDIA" | "ALTA" | "CRITICA";
  message: string;
  acknowledged: boolean;
  createdAt: string;
}

export interface LiveDeviceEvent {
  event: string;
  deviceId: number;
  status?: string;
  batteryLevel?: number;
}
