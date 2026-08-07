"use client";

import { useState } from "react";
import { AppShell } from "@/components/AppShell";
import { useCompanies, useCreateCompany } from "@/features/companies/hooks";

export default function CompaniesPage() {
  const { data: companies, isLoading, isError } = useCompanies();
  const createCompany = useCreateCompany();
  const [name, setName] = useState("");
  const [taxId, setTaxId] = useState("");

  async function handleCreate(event: React.FormEvent) {
    event.preventDefault();
    await createCompany.mutateAsync({ name, taxId });
    setName("");
    setTaxId("");
  }

  return (
    <AppShell>
      <h1 className="text-lg font-semibold mb-4">Administración de Empresas</h1>

      <form onSubmit={handleCreate} className="flex gap-2 mb-6">
        <input
          placeholder="Nombre"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
          className="rounded border border-black/10 dark:border-white/20 px-3 py-2 text-sm bg-transparent"
        />
        <input
          placeholder="RUC/NIT"
          value={taxId}
          onChange={(e) => setTaxId(e.target.value)}
          required
          className="rounded border border-black/10 dark:border-white/20 px-3 py-2 text-sm bg-transparent"
        />
        <button
          type="submit"
          disabled={createCompany.isPending}
          className="rounded bg-black text-white dark:bg-white dark:text-black px-4 py-2 text-sm font-medium disabled:opacity-50"
        >
          Crear
        </button>
      </form>

      {isLoading && <p className="text-sm">Cargando...</p>}
      {isError && <p className="text-sm text-red-600">No se pudieron cargar las empresas.</p>}

      <table className="w-full text-sm">
        <thead>
          <tr className="text-left border-b border-black/10 dark:border-white/10">
            <th className="py-2">Nombre</th>
            <th className="py-2">RUC/NIT</th>
            <th className="py-2">Estado</th>
          </tr>
        </thead>
        <tbody>
          {companies?.map((company) => (
            <tr key={company.id} className="border-b border-black/5 dark:border-white/5">
              <td className="py-2">{company.name}</td>
              <td className="py-2">{company.taxId}</td>
              <td className="py-2">{company.status}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </AppShell>
  );
}
