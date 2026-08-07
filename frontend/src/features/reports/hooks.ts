import { useMutation, useQuery } from "@tanstack/react-query";
import { downloadInventoryExport, fetchEventsReport, fetchInventoryReport } from "./api";
import { EventsQuery } from "./types";

export function useInventoryReport() {
  return useQuery({ queryKey: ["reports", "inventory"], queryFn: fetchInventoryReport });
}

export function useEventsReport(query: EventsQuery, enabled: boolean) {
  return useQuery({
    queryKey: ["reports", "events", query],
    queryFn: () => fetchEventsReport(query),
    enabled,
  });
}

export function useExportInventory() {
  return useMutation({ mutationFn: downloadInventoryExport });
}
