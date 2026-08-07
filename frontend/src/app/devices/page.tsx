"use client";

import { useState } from "react";
import { AppShell } from "@/components/AppShell";
import { useDevices, useRegisterDevice, useAssignDeviceGroup } from "@/features/devices/hooks";
import { useCreateDeviceGroup, useDeviceGroups } from "@/features/deviceGroups/hooks";

export default function DevicesPage() {
  const { data: devices } = useDevices();
  const { data: groups } = useDeviceGroups();
  const registerDevice = useRegisterDevice();
  const assignGroup = useAssignDeviceGroup();
  const createGroup = useCreateDeviceGroup();

  const [form, setForm] = useState({ imei: "", serialNumber: "", model: "", androidVersion: "" });
  const [groupName, setGroupName] = useState("");
  const [lastToken, setLastToken] = useState<{ imei: string; enrollmentToken: string } | null>(null);

  return (
    <AppShell>
      <h1 className="text-lg font-semibold mb-4">Gestión de Dispositivos</h1>

      <section className="mb-6">
        <h2 className="font-medium mb-2">Grupos</h2>
        <form
          className="flex gap-2 mb-2"
          onSubmit={async (e) => {
            e.preventDefault();
            await createGroup.mutateAsync({ name: groupName });
            setGroupName("");
          }}
        >
          <input placeholder="Nombre del grupo" required value={groupName}
            onChange={(e) => setGroupName(e.target.value)}
            className="rounded border border-black/10 dark:border-white/20 px-3 py-2 text-sm bg-transparent" />
          <button type="submit" disabled={createGroup.isPending}
            className="rounded bg-black text-white dark:bg-white dark:text-black px-4 py-2 text-sm font-medium disabled:opacity-50">
            Crear grupo
          </button>
        </form>
        <p className="text-sm text-black/60 dark:text-white/60">
          {groups?.map((g) => g.name).join(", ") || "Sin grupos aún"}
        </p>
      </section>

      <section className="mb-6">
        <h2 className="font-medium mb-2">Registrar dispositivo (pre-enrollment)</h2>
        <form
          className="flex flex-wrap gap-2 mb-2"
          onSubmit={async (e) => {
            e.preventDefault();
            const created = await registerDevice.mutateAsync(form);
            setLastToken({ imei: created.imei, enrollmentToken: created.enrollmentToken });
            setForm({ imei: "", serialNumber: "", model: "", androidVersion: "" });
          }}
        >
          <input placeholder="IMEI" required value={form.imei}
            onChange={(e) => setForm({ ...form, imei: e.target.value })}
            className="rounded border border-black/10 dark:border-white/20 px-3 py-2 text-sm bg-transparent" />
          <input placeholder="Número de serie" value={form.serialNumber}
            onChange={(e) => setForm({ ...form, serialNumber: e.target.value })}
            className="rounded border border-black/10 dark:border-white/20 px-3 py-2 text-sm bg-transparent" />
          <input placeholder="Modelo" value={form.model}
            onChange={(e) => setForm({ ...form, model: e.target.value })}
            className="rounded border border-black/10 dark:border-white/20 px-3 py-2 text-sm bg-transparent" />
          <input placeholder="Versión Android" value={form.androidVersion}
            onChange={(e) => setForm({ ...form, androidVersion: e.target.value })}
            className="rounded border border-black/10 dark:border-white/20 px-3 py-2 text-sm bg-transparent" />
          <button type="submit" disabled={registerDevice.isPending}
            className="rounded bg-black text-white dark:bg-white dark:text-black px-4 py-2 text-sm font-medium disabled:opacity-50">
            Registrar
          </button>
        </form>
        {lastToken && (
          <p className="text-sm bg-black/5 dark:bg-white/10 rounded p-2">
            Token de enrollment para <strong>{lastToken.imei}</strong>: <code>{lastToken.enrollmentToken}</code>
            <br />Ingrésalo en el agente Android para completar la inscripción (un solo uso).
          </p>
        )}
      </section>

      <section>
        <h2 className="font-medium mb-2">Inventario</h2>
        <table className="w-full text-sm">
          <thead>
            <tr className="text-left border-b border-black/10 dark:border-white/10">
              <th className="py-2">IMEI</th><th>Modelo</th><th>Estado</th><th>Última conexión</th><th>Grupo</th>
            </tr>
          </thead>
          <tbody>
            {devices?.map((device) => (
              <tr key={device.id} className="border-b border-black/5 dark:border-white/5">
                <td className="py-2">{device.imei}</td>
                <td>{device.model || "—"}</td>
                <td>
                  <span className={device.status === "ONLINE" ? "text-green-600" : "text-black/50 dark:text-white/50"}>
                    {device.enrollmentPending ? "PENDIENTE" : device.status}
                  </span>
                </td>
                <td>{device.lastSeenAt ? new Date(device.lastSeenAt).toLocaleString() : "—"}</td>
                <td>
                  <select
                    defaultValue={device.groupId ?? ""}
                    onChange={(e) => assignGroup.mutate({
                      deviceId: device.id,
                      groupId: e.target.value ? Number(e.target.value) : null,
                    })}
                    className="rounded border border-black/10 dark:border-white/20 bg-transparent text-sm px-2 py-1"
                  >
                    <option value="">Sin grupo</option>
                    {groups?.map((g) => <option key={g.id} value={g.id}>{g.name}</option>)}
                  </select>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </AppShell>
  );
}
