import { AppShell } from "./AppShell";

export function PlaceholderPage({ title, phase }: { title: string; phase: string }) {
  return (
    <AppShell>
      <h1 className="text-lg font-semibold mb-2">{title}</h1>
      <p className="text-sm text-black/60 dark:text-white/60">
        Este módulo se implementa en la {phase} del roadmap (ver
        docs/08_Plan_de_Implementación.md). Aún no tiene endpoints en el backend.
      </p>
    </AppShell>
  );
}
