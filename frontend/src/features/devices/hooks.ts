import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { assignDeviceGroup, registerDevice, fetchDevices } from "./api";

const DEVICES_KEY = ["devices"];

export function useDevices() {
  return useQuery({ queryKey: DEVICES_KEY, queryFn: fetchDevices, refetchInterval: 15000 });
}

export function useRegisterDevice() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: registerDevice,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: DEVICES_KEY }),
  });
}

export function useAssignDeviceGroup() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ deviceId, groupId }: { deviceId: number; groupId: number | null }) =>
      assignDeviceGroup(deviceId, groupId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: DEVICES_KEY }),
  });
}
