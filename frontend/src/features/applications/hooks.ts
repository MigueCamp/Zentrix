import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  fetchApplicationVersions, fetchApplications, installApplication, uninstallApplication, uploadApplication,
} from "./api";
import { InstallInput } from "./types";

const APPLICATIONS_KEY = ["applications"];

export function useApplications() {
  return useQuery({ queryKey: APPLICATIONS_KEY, queryFn: fetchApplications });
}

export function useUploadApplication() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: uploadApplication,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: APPLICATIONS_KEY }),
  });
}

export function useApplicationVersions(applicationId: number | null) {
  return useQuery({
    queryKey: [...APPLICATIONS_KEY, applicationId, "versions"],
    queryFn: () => fetchApplicationVersions(applicationId as number),
    enabled: applicationId != null,
    refetchInterval: 5000,
  });
}

export function useInstallApplication() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ applicationId, input }: { applicationId: number; input: InstallInput }) =>
      installApplication(applicationId, input),
    onSuccess: (_data, variables) =>
      queryClient.invalidateQueries({ queryKey: [...APPLICATIONS_KEY, variables.applicationId, "versions"] }),
  });
}

export function useUninstallApplication() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ applicationId, input }: { applicationId: number; input: InstallInput }) =>
      uninstallApplication(applicationId, input),
    onSuccess: (_data, variables) =>
      queryClient.invalidateQueries({ queryKey: [...APPLICATIONS_KEY, variables.applicationId, "versions"] }),
  });
}
