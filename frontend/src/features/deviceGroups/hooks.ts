import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createDeviceGroup, fetchDeviceGroups } from "./api";

const DEVICE_GROUPS_KEY = ["device-groups"];

export function useDeviceGroups() {
  return useQuery({ queryKey: DEVICE_GROUPS_KEY, queryFn: fetchDeviceGroups });
}

export function useCreateDeviceGroup() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createDeviceGroup,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: DEVICE_GROUPS_KEY }),
  });
}
