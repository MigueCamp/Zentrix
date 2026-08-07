"use client";

import { useState } from "react";
import { AppShell } from "@/components/AppShell";
import {
  useApiKeys, useCreateApiKey, useLogs, useRevokeApiKey, useSecuritySettings, useUpdateSecuritySettings,
} from "@/features/settings/hooks";
import { SecuritySettings } from "@/features/settings/types";

export default function SettingsPage() {
  const { data: settings } = useSecuritySettings();
  const updateSettings = useUpdateSecuritySettings();
  const { data: apiKeys } = useApiKeys();
  const createApiKey = useCreateApiKey();
  const revokeApiKey = useRevokeApiKey();
  const { data: logs } = useLogs();

  // Local edits (if any) override the server value; no effect needed to sync.
  const [edits, setEdits] = useState<Partial<SecuritySettings>>({});
  const form: SecuritySettings | null = settings ? { ...settings, ...edits } : null;
  const setForm = (next: SecuritySettings) => setEdits(next);
  const [keyName, setKeyName] = useState("");
  const [newKey, setNewKey] = useState<string | null>(null);

  return (
    <AppShell>
      <h1 className="text-lg font-semibold mb-4">Configuración</h1>

      <section className="mb-6">
        <h2 className="font-medium mb-2">Seguridad</h2>
        {form && (
          <form
            className="flex flex-col gap-2 max-w-md"
            onSubmit={async (e) => {
              e.preventDefault();
              await updateSettings.mutateAsync(form);
            }}
          >
            <label className="text-sm flex items-center justify-between gap-2">
              Longitud mínima de contraseña
              <input type="number" min={6} max={128} value={form.passwordMinLength}
                onChange={(e) => setForm({ ...form, passwordMinLength: Number(e.target.value) })}
                className="rounded border border-black/10 dark:border-white/20 px-3 py-1 text-sm bg-transparent w-24" />
            </label>
            <label className="text-sm flex items-center gap-2">
              <input type="checkbox" checked={form.requireUppercase}
                onChange={(e) => setForm({ ...form, requireUppercase: e.target.checked })} />
              Requerir mayúscula
            </label>
            <label className="text-sm flex items-center gap-2">
              <input type="checkbox" checked={form.requireDigit}
                onChange={(e) => setForm({ ...form, requireDigit: e.target.checked })} />
              Requerir dígito
            </label>
            <label className="text-sm flex items-center gap-2">
              <input type="checkbox" checked={form.requireSpecial}
                onChange={(e) => setForm({ ...form, requireSpecial: e.target.checked })} />
              Requerir carácter especial
            </label>
            <label className="text-sm flex items-center justify-between gap-2">
              Expiración de sesión (minutos)
              <input type="number" min={5} max={1440} value={form.sessionExpirationMinutes}
                onChange={(e) => setForm({ ...form, sessionExpirationMinutes: Number(e.target.value) })}
                className="rounded border border-black/10 dark:border-white/20 px-3 py-1 text-sm bg-transparent w-24" />
            </label>
            <button type="submit" disabled={updateSettings.isPending}
              className="rounded bg-black text-white dark:bg-white dark:text-black px-4 py-2 text-sm font-medium disabled:opacity-50 self-start">
              Guardar
            </button>
          </form>
        )}
      </section>

      <section className="mb-6">
        <h2 className="font-medium mb-2">API Keys</h2>
        <form
          className="flex gap-2 mb-2"
          onSubmit={async (e) => {
            e.preventDefault();
            const created = await createApiKey.mutateAsync(keyName);
            setNewKey(created.apiKey);
            setKeyName("");
          }}
        >
          <input placeholder="Nombre de la integración" required value={keyName}
            onChange={(e) => setKeyName(e.target.value)}
            className="rounded border border-black/10 dark:border-white/20 px-3 py-2 text-sm bg-transparent" />
          <button type="submit" disabled={createApiKey.isPending}
            className="rounded bg-black text-white dark:bg-white dark:text-black px-4 py-2 text-sm font-medium disabled:opacity-50">
            Generar
          </button>
        </form>
        {newKey && (
          <p className="text-sm bg-black/5 dark:bg-white/10 rounded p-2 mb-2">
            Clave generada (cópiala ahora, no se vuelve a mostrar): <code>{newKey}</code>
          </p>
        )}
        <table className="w-full text-sm">
          <thead>
            <tr className="text-left border-b border-black/10 dark:border-white/10">
              <th className="py-2">Nombre</th><th>Prefijo</th><th>Estado</th><th>Creada</th><th></th>
            </tr>
          </thead>
          <tbody>
            {apiKeys?.map((key) => (
              <tr key={key.id} className="border-b border-black/5 dark:border-white/5">
                <td className="py-2">{key.name}</td>
                <td className="font-mono text-xs">{key.prefix}…</td>
                <td>{key.active ? "Activa" : "Revocada"}</td>
                <td>{new Date(key.createdAt).toLocaleString()}</td>
                <td>
                  {key.active && (
                    <button onClick={() => revokeApiKey.mutate(key.id)} disabled={revokeApiKey.isPending}
                      className="rounded border border-black/20 dark:border-white/30 px-3 py-1 text-sm font-medium disabled:opacity-50">
                      Revocar
                    </button>
                  )}
                </td>
              </tr>
            ))}
            {!apiKeys?.length && (
              <tr><td colSpan={5} className="py-2 text-black/50 dark:text-white/50">Sin API keys</td></tr>
            )}
          </tbody>
        </table>
      </section>

      <section>
        <h2 className="font-medium mb-2">Logs de auditoría</h2>
        <table className="w-full text-sm">
          <thead>
            <tr className="text-left border-b border-black/10 dark:border-white/10">
              <th className="py-2">Usuario</th><th>Acción</th><th>Detalle</th><th>Fecha</th>
            </tr>
          </thead>
          <tbody>
            {logs?.content.map((log) => (
              <tr key={log.id} className="border-b border-black/5 dark:border-white/5">
                <td className="py-2">{log.userEmail || "—"}</td>
                <td>{log.action}</td>
                <td className="font-mono text-xs">{log.detailJson}</td>
                <td>{new Date(log.actionDate).toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </AppShell>
  );
}
