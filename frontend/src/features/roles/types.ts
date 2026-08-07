export interface Permission {
  id: number;
  code: string;
  description: string;
}

export interface Role {
  id: number;
  name: string;
  permissions: Permission[];
}

export interface RoleInput {
  name: string;
}
