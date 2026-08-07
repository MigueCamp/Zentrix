export interface Application {
  id: number;
  name: string;
  packageName: string;
  currentVersion: string;
  fileUrl: string;
  uploadedAt: string;
}

export interface ApplicationUploadInput {
  name: string;
  packageName: string;
  version: string;
  file: File;
}

export interface InstallInput {
  deviceId?: number | null;
  groupId?: number | null;
}

export interface DeviceApplicationStatus {
  deviceId: number;
  deviceImei: string;
  installedVersion: string | null;
  status: "PENDIENTE" | "INSTALADA" | "ERROR" | "DESINSTALADA";
  updatedAt: string;
}
