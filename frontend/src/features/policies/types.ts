export type PolicyType = "WIFI" | "VPN" | "KIOSCO" | "RESTRICCIONES";

export interface Policy {
  id: number;
  name: string;
  type: PolicyType;
  configurationJson: string;
  encrypted: boolean;
  updatedAt: string;
}

export interface PolicyInput {
  name: string;
  type: PolicyType;
  configurationJson: string;
}

export interface PolicyAssignment {
  id: number;
  policyId: number;
  policyName: string;
  policyType: PolicyType;
  deviceId: number | null;
  groupId: number | null;
  assignedAt: string;
}

export interface PolicyAssignInput {
  deviceId?: number | null;
  groupId?: number | null;
}
