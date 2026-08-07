import { apiClient } from "@/services/api-client";
import { AuditLogPage } from "./types";

export async function fetchAuditLogs(): Promise<AuditLogPage> {
  const { data } = await apiClient.get<AuditLogPage>("/audit-logs");
  return data;
}
