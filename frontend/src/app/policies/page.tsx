"use client";

import { useState } from "react";
import { AppShell } from "@/components/AppShell";
import { useAssignPolicy, useCreatePolicy, usePolicies, usePolicyAssignments } from "@/features/policies/hooks";
import { useDevices } from "@/features/devices/hooks";
import { useDeviceGroups } from "@/features/deviceGroups/hooks";
import { PolicyType } from "@/features/policies/types";

const TYPE_LABELS: Record<PolicyType, string> = {
  WIFI: "WiFi", VPN: "VPN", KIOSCO: "Modo Kiosco", RESTRICCIONES: "Restricciones",
};

function defaultFieldsFor(type: PolicyType): Record<string, string | boolean> {
  switch (type) {
    case "WIFI": return { ssid: "", security: "WPA2", password: "" };
    case "VPN": return { server: "", user: "", password: "" };
    case "KIOSCO": return { allowedPackages: "" };
    case "RESTRICCIONES": return { camera: false, usb: false, unknownSources: false };
  }
}

function buildConfigurationJson(type: PolicyType, fields: Record<string, string | boolean>): string {
  if (type === "KIOSCO") {
    const packages = String(fields.allowedPackages || "").split(",").map((p) => p.trim()).filter(Boolean);
    return JSON.stringify({ allowedPackages: packages });
  }
  return JSON.stringify(fields);
}

export default function PoliciesPage() {
  const { data: policies } = usePolicies();
  const { data: assignments } = usePolicyAssignments();
  const { data: devices } = useDevices();
  const { data: groups } = useDeviceGroups();
  const createPolicy = useCreatePolicy();
  const assignPolicy = useAssignPolicy();

  const [type, setType] = useState<PolicyType>("WIFI");
  const [name, setName] = useState("");
  const [fields, setFields] = useState<Record<string, string | boolean>>(defaultFieldsFor("WIFI"));
  const [assignTarget, setAssignTarget] = useState<Record<number, string>>({});

  function updateType(nextType: PolicyType) {
    setType(nextType);
    setFields(defaultFieldsFor(nextType));
  }

  async function handleAssign(policyId: number) {
    const value = assignTarget[policyId];
    if (!value) return;
    const [kind, id] = value.split(":");
    await assignPolicy.mutateAsync({
      policyId,
      input: kind === "device" ? { deviceId: Number(id) } : { groupId: Number(id) },
    });
  }

  return (
    <AppShell>
      <h1 className="text-lg font-semibold mb-4">Perfiles y Políticas</h1>

      <section className="mb-6">
        <h2 className="font-medium mb-2">Nuevo perfil</h2>
        <form
          className="flex flex-col gap-2 max-w-md"
          onSubmit={async (e) => {
            e.preventDefault();
            await createPolicy.mutateAsync({ name, type, configurationJson: buildConfigurationJson(type, fields) });
            setName("");
            setFields(defaultFieldsFor(type));
          }}
        >
          <input placeholder="Nombre del perfil" required value={name} onChange={(e) => setName(e.target.value)}
            className="rounded border border-black/10 dark:border-white/20 px-3 py-2 text-sm bg-transparent" />
          <select value={type} onChange={(e) => updateType(e.target.value as PolicyType)}
            className="rounded border border-black/10 dark:border-white/20 bg-transparent text-sm px-3 py-2">
            {Object.entries(TYPE_LABELS).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
          </select>

          {type === "WIFI" && (
            <>
              <input placeholder="SSID" required value={fields.ssid as string}
                onChange={(e) => setFields({ ...fields, ssid: e.target.value })}
                className="rounded border border-black/10 dark:border-white/20 px-3 py-2 text-sm bg-transparent" />
              <input placeholder="Contraseña" type="password" required value={fields.password as string}
                onChange={(e) => setFields({ ...fields, password: e.target.value })}
                className="rounded border border-black/10 dark:border-white/20 px-3 py-2 text-sm bg-transparent" />
            </>
          )}
          {type === "VPN" && (
            <>
              <input placeholder="Servidor VPN" required value={fields.server as string}
                onChange={(e) => setFields({ ...fields, server: e.target.value })}
                className="rounded border border-black/10 dark:border-white/20 px-3 py-2 text-sm bg-transparent" />
              <input placeholder="Usuario" required value={fields.user as string}
                onChange={(e) => setFields({ ...fields, user: e.target.value })}
                className="rounded border border-black/10 dark:border-white/20 px-3 py-2 text-sm bg-transparent" />
              <input placeholder="Contraseña" type="password" required value={fields.password as string}
                onChange={(e) => setFields({ ...fields, password: e.target.value })}
                className="rounded border border-black/10 dark:border-white/20 px-3 py-2 text-sm bg-transparent" />
            </>
          )}
          {type === "KIOSCO" && (
            <input placeholder="Paquetes permitidos (separados por coma)" value={fields.allowedPackages as string}
              onChange={(e) => setFields({ ...fields, allowedPackages: e.target.value })}
              className="rounded border border-black/10 dark:border-white/20 px-3 py-2 text-sm bg-transparent" />
          )}
          {type === "RESTRICCIONES" && (
            <div className="flex flex-col gap-1 text-sm">
              {(["camera", "usb", "unknownSources"] as const).map((key) => (
                <label key={key} className="flex items-center gap-2">
                  <input type="checkbox" checked={fields[key] as boolean}
                    onChange={(e) => setFields({ ...fields, [key]: e.target.checked })} />
                  Deshabilitar {key === "camera" ? "cámara" : key === "usb" ? "USB" : "fuentes desconocidas"}
                </label>
              ))}
            </div>
          )}

          <button type="submit" disabled={createPolicy.isPending}
            className="rounded bg-black text-white dark:bg-white dark:text-black px-4 py-2 text-sm font-medium disabled:opacity-50 self-start">
            Crear perfil
          </button>
        </form>
      </section>

      <section>
        <h2 className="font-medium mb-2">Perfiles y asignación</h2>
        <table className="w-full text-sm">
          <thead>
            <tr className="text-left border-b border-black/10 dark:border-white/10">
              <th className="py-2">Nombre</th><th>Tipo</th><th>Cifrado</th><th>Asignar a</th><th>Asignaciones actuales</th>
            </tr>
          </thead>
          <tbody>
            {policies?.map((policy) => (
              <tr key={policy.id} className="border-b border-black/5 dark:border-white/5">
                <td className="py-2">{policy.name}</td>
                <td>{TYPE_LABELS[policy.type]}</td>
                <td>{policy.encrypted ? "Sí" : "No"}</td>
                <td>
                  <div className="flex gap-2">
                    <select
                      value={assignTarget[policy.id] ?? ""}
                      onChange={(e) => setAssignTarget({ ...assignTarget, [policy.id]: e.target.value })}
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
                    <button onClick={() => handleAssign(policy.id)} disabled={assignPolicy.isPending}
                      className="rounded bg-black text-white dark:bg-white dark:text-black px-3 py-1 text-sm font-medium disabled:opacity-50">
                      Asignar
                    </button>
                  </div>
                </td>
                <td>
                  {assignments
                    ?.filter((a) => a.policyId === policy.id)
                    .map((a) => a.deviceId ? `Dispositivo #${a.deviceId}` : `Grupo #${a.groupId}`)
                    .join(", ") || "—"}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </AppShell>
  );
}
