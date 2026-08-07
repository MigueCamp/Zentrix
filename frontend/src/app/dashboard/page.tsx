import { AppShell } from "@/components/AppShell";

export default function DashboardPage() {
  return (
    <AppShell>
      <h1 className="text-lg font-semibold mb-2">Dashboard</h1>
      <p className="text-sm text-black/60 dark:text-white/60">
        Bienvenido a Zentrix. Usa el menú lateral para administrar empresas, usuarios,
        dispositivos y el resto de módulos de la plataforma.
      </p>
    </AppShell>
  );
}
