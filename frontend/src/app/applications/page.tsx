"use client";

import { useState } from "react";
import { AppShell } from "@/components/AppShell";
import {
  useApplicationVersions, useApplications, useInstallApplication, useUninstallApplication, useUploadApplication,
} from "@/features/applications/hooks";
import { useDevices } from "@/features/devices/hooks";
import { useDeviceGroups } from "@/features/deviceGroups/hooks";

export default function ApplicationsPage() {
  const { data: applications } = useApplications();
  const { data: devices } = useDevices();
  const { data: groups } = useDeviceGroups();
  const uploadApplication = useUploadApplication();
  const installApplication = useInstallApplication();
  const uninstallApplication = useUninstallApplication();

  const [form, setForm] = useState({ name: "", packageName: "", version: "" });
  const [file, setFile] = useState<File | null>(null);
  const [target, setTarget] = useState<Record<number, string>>({});
  const [selectedAppId, setSelectedAppId] = useState<number | null>(null);
  const { data: versions } = useApplicationVersions(selectedAppId);

  async function handleInstallOrUninstall(applicationId: number, action: "install" | "uninstall") {
    const value = target[applicationId];
    if (!value) return;
    const [kind, id] = value.split(":");
    const input = kind === "device" ? { deviceId: Number(id) } : { groupId: Number(id) };
    if (action === "install") {
      await installApplication.mutateAsync({ applicationId, input });
    } else {
      await uninstallApplication.mutateAsync({ applicationId, input });
    }
    setSelectedAppId(applicationId);
  }

  return (
    <AppShell>
      <h1 className="text-lg font-semibold mb-4">Aplicaciones</h1>

      <section className="mb-6">
        <h2 className="font-medium mb-2">Subir APK</h2>
        <form
          className="flex flex-wrap gap-2 items-center"
          onSubmit={async (e) => {
            e.preventDefault();
            if (!file) return;
            await uploadApplication.mutateAsync({ ...form, file });
            setForm({ name: "", packageName: "", version: "" });
            setFile(null);
            (e.target as HTMLFormElement).reset();
          }}
        >
          <input placeholder="Nombre" required value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            className="rounded border border-black/10 dark:border-white/20 px-3 py-2 text-sm bg-transparent" />
          <input placeholder="Package name (com.empresa.app)" required value={form.packageName}
            onChange={(e) => setForm({ ...form, packageName: e.target.value })}
            className="rounded border border-black/10 dark:border-white/20 px-3 py-2 text-sm bg-transparent" />
          <input placeholder="Versión" required value={form.version}
            onChange={(e) => setForm({ ...form, version: e.target.value })}
            className="rounded border border-black/10 dark:border-white/20 px-3 py-2 text-sm bg-transparent" />
          <input type="file" accept=".apk" required onChange={(e) => setFile(e.target.files?.[0] ?? null)}
            className="text-sm" />
          <button type="submit" disabled={uploadApplication.isPending || !file}
            className="rounded bg-black text-white dark:bg-white dark:text-black px-4 py-2 text-sm font-medium disabled:opacity-50">
            Subir
          </button>
        </form>
      </section>

      <section>
        <h2 className="font-medium mb-2">Catálogo</h2>
        <table className="w-full text-sm">
          <thead>
            <tr className="text-left border-b border-black/10 dark:border-white/10">
              <th className="py-2">Nombre</th><th>Package</th><th>Versión</th><th>Distribuir a</th><th></th>
            </tr>
          </thead>
          <tbody>
            {applications?.map((app) => (
              <tr key={app.id} className="border-b border-black/5 dark:border-white/5">
                <td className="py-2">{app.name}</td>
                <td>{app.packageName}</td>
                <td>{app.currentVersion}</td>
                <td>
                  <select
                    value={target[app.id] ?? ""}
                    onChange={(e) => setTarget({ ...target, [app.id]: e.target.value })}
                    className="rounded border border-black/10 dark:border-white/20 bg-transparent text-sm px-2 py-1"
                  >
                    <option value="">Seleccionar destino</option>
                    <optgroup label="Dispositivos">
                      {devices?.map((d) => <option key={`device:${d.id}`} value={`device:${d.id}`}>{d.imei}</option>)}
                    </optgroup>
                    <optgroup label="Grupos">
                      {groups?.map((g) => <option key={`group:${g.id}`} value={`group:${g.id}`}>{g.name}</option>)}
                    </optgroup>
                  </select>
                </td>
                <td>
                  <div className="flex gap-2">
                    <button onClick={() => handleInstallOrUninstall(app.id, "install")}
                      disabled={installApplication.isPending}
                      className="rounded bg-black text-white dark:bg-white dark:text-black px-3 py-1 text-sm font-medium disabled:opacity-50">
                      Instalar
                    </button>
                    <button onClick={() => handleInstallOrUninstall(app.id, "uninstall")}
                      disabled={uninstallApplication.isPending}
                      className="rounded border border-black/20 dark:border-white/30 px-3 py-1 text-sm font-medium disabled:opacity-50">
                      Desinstalar
                    </button>
                    <button onClick={() => setSelectedAppId(app.id)}
                      className="text-sm underline text-black/60 dark:text-white/60">
                      Ver estado
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      {selectedAppId != null && (
        <section className="mt-6">
          <h2 className="font-medium mb-2">Estado de instalación — Aplicación #{selectedAppId}</h2>
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left border-b border-black/10 dark:border-white/10">
                <th className="py-2">IMEI</th><th>Versión instalada</th><th>Estado</th>
              </tr>
            </thead>
            <tbody>
              {versions?.map((v) => (
                <tr key={v.deviceId} className="border-b border-black/5 dark:border-white/5">
                  <td className="py-2">{v.deviceImei}</td>
                  <td>{v.installedVersion || "—"}</td>
                  <td>{v.status}</td>
                </tr>
              ))}
              {!versions?.length && (
                <tr><td colSpan={3} className="py-2 text-black/50 dark:text-white/50">Sin registros aún</td></tr>
              )}
            </tbody>
          </table>
        </section>
      )}
    </AppShell>
  );
}
