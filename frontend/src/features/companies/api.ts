import { apiClient } from "@/services/api-client";
import { Company, CompanyInput } from "./types";

export async function fetchCompanies(): Promise<Company[]> {
  const { data } = await apiClient.get<Company[]>("/companies");
  return data;
}

export async function createCompany(input: CompanyInput): Promise<Company> {
  const { data } = await apiClient.post<Company>("/companies", input);
  return data;
}
