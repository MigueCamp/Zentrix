import { apiClient } from "@/services/api-client";
import { Permission, Role, RoleInput } from "./types";

export async function fetchRoles(): Promise<Role[]> {
  const { data } = await apiClient.get<Role[]>("/roles");
  return data;
}

export async function createRole(input: RoleInput): Promise<Role> {
  const { data } = await apiClient.post<Role>("/roles", input);
  return data;
}

export async function fetchPermissions(): Promise<Permission[]> {
  const { data } = await apiClient.get<Permission[]>("/permissions");
  return data;
}

export async function assignRolePermissions(roleId: number, permissionIds: number[]): Promise<Role> {
  const { data } = await apiClient.put<Role>(`/roles/${roleId}/permissions`, { permissionIds });
  return data;
}
