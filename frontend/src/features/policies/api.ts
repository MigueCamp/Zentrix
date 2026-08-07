import { apiClient } from "@/services/api-client";
import { Policy, PolicyAssignInput, PolicyAssignment, PolicyInput } from "./types";

export async function fetchPolicies(): Promise<Policy[]> {
  const { data } = await apiClient.get<Policy[]>("/policies");
  return data;
}

export async function createPolicy(input: PolicyInput): Promise<Policy> {
  const { data } = await apiClient.post<Policy>("/policies", input);
  return data;
}

export async function fetchPolicyAssignments(): Promise<PolicyAssignment[]> {
  const { data } = await apiClient.get<PolicyAssignment[]>("/policies/assignments");
  return data;
}

export async function assignPolicy(policyId: number, input: PolicyAssignInput): Promise<PolicyAssignment> {
  const { data } = await apiClient.post<PolicyAssignment>(`/policies/${policyId}/assign`, input);
  return data;
}
