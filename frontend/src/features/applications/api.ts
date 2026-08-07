import { apiClient } from "@/services/api-client";
import { Application, ApplicationUploadInput, DeviceApplicationStatus, InstallInput } from "./types";

export async function fetchApplications(): Promise<Application[]> {
  const { data } = await apiClient.get<Application[]>("/applications");
  return data;
}

export async function uploadApplication(input: ApplicationUploadInput): Promise<Application> {
  const form = new FormData();
  form.append("name", input.name);
  form.append("packageName", input.packageName);
  form.append("version", input.version);
  form.append("file", input.file);
  const { data } = await apiClient.post<Application>("/applications", form, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  return data;
}

export async function installApplication(applicationId: number, input: InstallInput): Promise<DeviceApplicationStatus> {
  const { data } = await apiClient.post<DeviceApplicationStatus>(`/applications/${applicationId}/install`, input);
  return data;
}

export async function uninstallApplication(applicationId: number, input: InstallInput): Promise<void> {
  await apiClient.post(`/applications/${applicationId}/uninstall`, input);
}

export async function fetchApplicationVersions(applicationId: number): Promise<DeviceApplicationStatus[]> {
  const { data } = await apiClient.get<DeviceApplicationStatus[]>(`/applications/${applicationId}/versions`);
  return data;
}
