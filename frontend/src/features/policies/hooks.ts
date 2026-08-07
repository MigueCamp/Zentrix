import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { assignPolicy, createPolicy, fetchPolicies, fetchPolicyAssignments } from "./api";
import { PolicyAssignInput } from "./types";

const POLICIES_KEY = ["policies"];
const ASSIGNMENTS_KEY = ["policy-assignments"];

export function usePolicies() {
  return useQuery({ queryKey: POLICIES_KEY, queryFn: fetchPolicies });
}

export function useCreatePolicy() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createPolicy,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: POLICIES_KEY }),
  });
}

export function usePolicyAssignments() {
  return useQuery({ queryKey: ASSIGNMENTS_KEY, queryFn: fetchPolicyAssignments });
}

export function useAssignPolicy() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ policyId, input }: { policyId: number; input: PolicyAssignInput }) => assignPolicy(policyId, input),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ASSIGNMENTS_KEY }),
  });
}
