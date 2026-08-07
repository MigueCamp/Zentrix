import { apiClient } from "@/services/api-client";
import { ApiKey, ApiKeyCreated, AuditLogEntry, SecuritySettings } from "./types";

export async function fetchSecuritySettings(): Promise<SecuritySettings> {
  const { data } = await apiClient.get<SecuritySettings>("/settings/security");
  return data;
}

export async function updateSecuritySettings(settings: SecuritySettings): Promise<SecuritySettings> {
  const { data } = await apiClient.put<SecuritySettings>("/settings/security", settings);
  return data;
}

export async function fetchApiKeys(): Promise<ApiKey[]> {
  const { data } = await apiClient.get<ApiKey[]>("/settings/api-keys");
  return data;
}

export async function createApiKey(name: string): Promise<ApiKeyCreated> {
  const { data } = await apiClient.post<ApiKeyCreated>("/settings/api-keys", { name });
  return data;
}

export async function revokeApiKey(id: number): Promise<void> {
  await apiClient.delete(`/settings/api-keys/${id}`);
}

export async function fetchLogs(): Promise<{ content: AuditLogEntry[] }> {
  const { data } = await apiClient.get<{ content: AuditLogEntry[] }>("/settings/logs", { params: { size: 30 } });
  return data;
}
