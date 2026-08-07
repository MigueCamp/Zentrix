"use client";

import { AppShell } from "@/components/AppShell";
import { useDevices } from "@/features/devices/hooks";
import { useAcknowledgeAlert, useAlerts, useDeviceLocation, useDeviceStatus, useLiveDeviceEvents } from "@/features/monitoring/hooks";
import { Device } from "@/features/devices/types";

function formatBytes(bytes: number | null): string {
  if (bytes == null) return "—";
  const gb = bytes / (1024 * 1024 * 1024);
  return gb >= 1 ? `${gb.toFixed(1)} GB` : `${(bytes / (1024 * 1024)).toFixed(0)} MB`;
}

function DeviceStatusRow({ device }: { device: Device }) {
  const { data: status } = useDeviceStatus(device.id);
  const { data: location } = useDeviceLocation(device.id);

  return (
    <tr className="border-b border-black/5 dark:border-white/5">
      <td className="py-2">{device.imei}</td>
      <td>
        <span className={status?.online ? "text-green-600" : "text-black/50 dark:text-white/50"}>
          {status?.online ? "ONLINE" : "OFFLINE"}
        </span>
      </td>
      <td>{status?.batteryLevel != null ? `${status.batteryLevel}%` : "—"}</td>
      <td>{formatBytes(status?.storageFreeBytes ?? null)}</td>
      <td>
        {status?.memoryUsedBytes != null && status?.memoryTotalBytes != null
          ? `${formatBytes(status.memoryUsedBytes)} / ${formatBytes(status.memoryTotalBytes)}`
          : "—"}
      </td>
      <td>
        {location?.latitude != null ? `${location.latitude.toFixed(4)}, ${location.longitude?.toFixed(4)}` : "—"}
      </td>
      <td>{status?.lastSeenAt ? new Date(status.lastSeenAt).toLocaleString() : "—"}</td>
    </tr>
  );
}

export default function MonitoringPage() {
  const { data: devices } = useDevices();
  const { data: alerts } = useAlerts(false);
  const acknowledgeAlert = useAcknowledgeAlert();
  const { events, connected } = useLiveDeviceEvents();

  return (
    <AppShell>
      <h1 className="text-lg font-semibold mb-4">Monitoreo</h1>

      <section className="mb-6">
        <h2 className="font-medium mb-2">Estado de la flota</h2>
        <table className="w-full text-sm">
          <thead>
            <tr className="text-left border-b border-black/10 dark:border-white/10">
              <th className="py-2">IMEI</th><th>Estado</th><th>Batería</th><th>Almacenamiento libre</th>
              <th>Memoria</th><th>Ubicación</th><th>Última conexión</th>
            </tr>
          </thead>
          <tbody>
            {devices?.map((device) => <DeviceStatusRow key={device.id} device={device} />)}
          </tbody>
        </table>
      </section>

      <section className="mb-6">
        <h2 className="font-medium mb-2">Alertas pendientes</h2>
        <table className="w-full text-sm">
          <thead>
            <tr className="text-left border-b border-black/10 dark:border-white/10">
              <th className="py-2">Dispositivo</th><th>Tipo</th><th>Severidad</th><th>Mensaje</th><th>Fecha</th><th></th>
            </tr>
          </thead>
          <tbody>
            {alerts?.content.map((alert) => (
              <tr key={alert.id} className="border-b border-black/5 dark:border-white/5">
                <td className="py-2">{alert.deviceImei}</td>
                <td>{alert.type}</td>
                <td>{alert.severity}</td>
                <td>{alert.message}</td>
                <td>{new Date(alert.createdAt).toLocaleString()}</td>
                <td>
                  <button onClick={() => acknowledgeAlert.mutate(alert.id)} disabled={acknowledgeAlert.isPending}
                    className="rounded border border-black/20 dark:border-white/30 px-3 py-1 text-sm font-medium disabled:opacity-50">
                    Atender
                  </button>
                </td>
              </tr>
            ))}
            {!alerts?.content.length && (
              <tr><td colSpan={6} className="py-2 text-black/50 dark:text-white/50">Sin alertas pendientes</td></tr>
            )}
          </tbody>
        </table>
      </section>

      <section>
        <h2 className="font-medium mb-2">
          Actividad en vivo{" "}
          <span className={connected ? "text-green-600 text-xs" : "text-black/40 dark:text-white/40 text-xs"}>
            {connected ? "● conectado" : "○ desconectado"}
          </span>
        </h2>
        <ul className="text-sm space-y-1">
          {events.map((event, index) => (
            <li key={index} className="text-black/70 dark:text-white/70">
              Dispositivo #{event.deviceId} — {event.event}
              {event.status ? ` (${event.status})` : ""}
              {event.batteryLevel != null ? `, batería ${event.batteryLevel}%` : ""}
            </li>
          ))}
          {!events.length && <li className="text-black/50 dark:text-white/50">Esperando eventos…</li>}
        </ul>
      </section>
    </AppShell>
  );
}
