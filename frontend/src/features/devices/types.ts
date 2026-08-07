export interface Device {
  id: number;
  imei: string;
  serialNumber: string | null;
  model: string | null;
  androidVersion: string | null;
  status: "ONLINE" | "OFFLINE" | "BLOQUEADO" | "ELIMINADO";
  lastSeenAt: string | null;
  registeredAt: string;
  groupId: number | null;
  groupName: string | null;
  enrollmentPending: boolean;
}

export interface DeviceInput {
  imei: string;
  serialNumber?: string;
  model?: string;
  androidVersion?: string;
}

export interface DeviceCreated {
  id: number;
  imei: string;
  enrollmentToken: string;
}
