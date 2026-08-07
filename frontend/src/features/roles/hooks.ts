import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { assignRolePermissions, createRole, fetchPermissions, fetchRoles } from "./api";

const ROLES_KEY = ["roles"];
const PERMISSIONS_KEY = ["permissions"];

export function useRoles() {
  return useQuery({ queryKey: ROLES_KEY, queryFn: fetchRoles });
}

export function usePermissions() {
  return useQuery({ queryKey: PERMISSIONS_KEY, queryFn: fetchPermissions });
}

export function useCreateRole() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createRole,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ROLES_KEY }),
  });
}

export function useAssignRolePermissions() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ roleId, permissionIds }: { roleId: number; permissionIds: number[] }) =>
      assignRolePermissions(roleId, permissionIds),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ROLES_KEY }),
  });
}
