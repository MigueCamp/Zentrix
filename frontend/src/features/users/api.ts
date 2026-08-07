import { apiClient } from "@/services/api-client";
import { AppUser, UserInput } from "./types";

export async function fetchUsers(): Promise<AppUser[]> {
  const { data } = await apiClient.get<AppUser[]>("/users");
  return data;
}

export async function createUser(input: UserInput): Promise<AppUser> {
  const { data } = await apiClient.post<AppUser>("/users", input);
  return data;
}

export async function assignUserRoles(userId: number, roleIds: number[]): Promise<AppUser> {
  const { data } = await apiClient.post<AppUser>(`/users/${userId}/roles`, { roleIds });
  return data;
}
