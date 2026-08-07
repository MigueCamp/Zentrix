import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  createApiKey, fetchApiKeys, fetchLogs, fetchSecuritySettings, revokeApiKey, updateSecuritySettings,
} from "./api";

const API_KEYS_KEY = ["api-keys"];
const SECURITY_KEY = ["security-settings"];

export function useSecuritySettings() {
  return useQuery({ queryKey: SECURITY_KEY, queryFn: fetchSecuritySettings });
}

export function useUpdateSecuritySettings() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: updateSecuritySettings,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: SECURITY_KEY }),
  });
}

export function useApiKeys() {
  return useQuery({ queryKey: API_KEYS_KEY, queryFn: fetchApiKeys });
}

export function useCreateApiKey() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createApiKey,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: API_KEYS_KEY }),
  });
}

export function useRevokeApiKey() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: revokeApiKey,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: API_KEYS_KEY }),
  });
}

export function useLogs() {
  return useQuery({ queryKey: ["settings-logs"], queryFn: fetchLogs });
}
