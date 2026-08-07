import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createCompany, fetchCompanies } from "./api";

const COMPANIES_KEY = ["companies"];

export function useCompanies() {
  return useQuery({ queryKey: COMPANIES_KEY, queryFn: fetchCompanies });
}

export function useCreateCompany() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createCompany,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: COMPANIES_KEY }),
  });
}
