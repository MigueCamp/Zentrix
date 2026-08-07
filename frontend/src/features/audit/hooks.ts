import { useQuery } from "@tanstack/react-query";
import { fetchAuditLogs } from "./api";

export function useAuditLogs() {
  return useQuery({ queryKey: ["audit-logs"], queryFn: fetchAuditLogs });
}
