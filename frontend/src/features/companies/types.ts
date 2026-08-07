export interface Company {
  id: number;
  name: string;
  taxId: string;
  status: "ACTIVA" | "SUSPENDIDA" | "ELIMINADA";
  createdAt: string;
}

export interface CompanyInput {
  name: string;
  taxId: string;
}
