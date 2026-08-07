export interface AppUser {
  id: number;
  name: string;
  email: string;
  status: "ACTIVO" | "INACTIVO";
  createdAt: string;
  roles: string[];
}

export interface UserInput {
  name: string;
  email: string;
  password: string;
}
