import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { assignUserRoles, createUser, fetchUsers } from "./api";

const USERS_KEY = ["users"];

export function useUsers() {
  return useQuery({ queryKey: USERS_KEY, queryFn: fetchUsers });
}

export function useCreateUser() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createUser,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: USERS_KEY }),
  });
}

export function useAssignUserRoles() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ userId, roleIds }: { userId: number; roleIds: number[] }) =>
      assignUserRoles(userId, roleIds),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: USERS_KEY }),
  });
}
