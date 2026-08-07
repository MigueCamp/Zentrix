"use client";

import { useState } from "react";
import { AppShell } from "@/components/AppShell";
import { useEventsReport, useExportInventory, useInventoryReport } from "@/features/reports/hooks";

function toIsoWithSeconds(datetimeLocalValue: string): string {
  return datetimeLocalValue.length === 16 ? `${datetimeLocalValue}:00` : datetimeLocalValue;
}

export default function ReportsPage() {
  const { data: inventory } = useInventoryReport();
  const exportInventory = useExportInventory();

  const [from, setFrom] = useState("2026-01-01T00:00");
  const [to, setTo] = useState("2026-12-31T23:59");
  const [type, setType] = useState("");
  const [queryEnabled, setQueryEnabled] = useState(false);
  const eventsQuery = { from: toIsoWithSeconds(from), to: toIsoWithSeconds(to), type: type || undefined };
  const { data: events } = useEventsReport(eventsQuery, queryEnabled);

  return (
    <AppShell>
      <h1 className="text-lg font-semibold mb-4">Reportes</h1>

      <section className="mb-6">
        <div className="flex items-center justify-between mb-2">
          <h2 className="font-medium">Inventario</h2>
          <div className="flex gap-2">
            <button onClick={() => exportInventory.mutate("pdf")} disabled={exportInventory.isPending}
              className="rounded border border-black/20 dark:border-white/30 px-3 py-1 text-sm font-medium disabled:opacity-50">
              Exportar PDF
            </button>
            <button onClick={() => exportInventory.mutate("xlsx")} disabled={exportInventory.isPending}
              className="rounded border border-black/20 dark:border-white/30 px-3 py-1 text-sm font-medium disabled:opacity-50">
              Exportar Excel
            </button>
          </div>
        </div>
        <table className="w-full text-sm">
          <thead>
            <tr className="text-left border-b border-black/10 dark:border-white/10">
              <th className="py-2">IMEI</th><th>Modelo</th><th>Versión Android</th><th>Estado</th><th>Grupo</th><th>Última conexión</th>
            </tr>
          </thead>
          <tbody>
            {inventory?.map((device) => (
              <tr key={device.id} className="border-b border-black/5 dark:border-white/5">
                <td className="py-2">{device.imei}</td>
                <td>{device.model || "—"}</td>
                <td>{device.androidVersion || "—"}</td>
                <td>{device.status}</td>
                <td>{device.groupName || "—"}</td>
                <td>{device.lastSeenAt ? new Date(device.lastSeenAt).toLocaleString() : "—"}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section>
        <h2 className="font-medium mb-2">Eventos</h2>
        <form
          className="flex flex-wrap gap-2 items-end mb-3"
          onSubmit={(e) => {
            e.preventDefault();
            setQueryEnabled(true);
          }}
        >
          <label className="text-sm flex flex-col gap-1">
            Desde
            <input type="datetime-local" value={from} onChange={(e) => setFrom(e.target.value)}
              className="rounded border border-black/10 dark:border-white/20 px-3 py-2 text-sm bg-transparent" />
          </label>
          <label className="text-sm flex flex-col gap-1">
            Hasta
            <input type="datetime-local" value={to} onChange={(e) => setTo(e.target.value)}
              className="rounded border border-black/10 dark:border-white/20 px-3 py-2 text-sm bg-transparent" />
          </label>
          <label className="text-sm flex flex-col gap-1">
            Tipo
            <select value={type} onChange={(e) => setType(e.target.value)}
              className="rounded border border-black/10 dark:border-white/20 bg-transparent text-sm px-3 py-2">
              <option value="">Todos</option>
              <option value="BATERIA">Batería</option>
              <option value="ALMACENAMIENTO">Almacenamiento</option>
              <option value="MEMORIA">Memoria</option>
              <option value="UBICACION">Ubicación</option>
              <option value="CONEXION">Conexión</option>
            </select>
          </label>
          <button type="submit"
            className="rounded bg-black text-white dark:bg-white dark:text-black px-4 py-2 text-sm font-medium">
            Consultar
          </button>
        </form>

        <table className="w-full text-sm">
          <thead>
            <tr className="text-left border-b border-black/10 dark:border-white/10">
              <th className="py-2">Dispositivo</th><th>Tipo</th><th>Detalle</th><th>Fecha</th>
            </tr>
          </thead>
          <tbody>
            {events?.content.map((row) => (
              <tr key={row.id} className="border-b border-black/5 dark:border-white/5">
                <td className="py-2">{row.deviceImei}</td>
                <td>{row.type}</td>
                <td className="font-mono text-xs">{row.valueJson}</td>
                <td>{new Date(row.eventDate).toLocaleString()}</td>
              </tr>
            ))}
            {queryEnabled && !events?.content.length && (
              <tr><td colSpan={4} className="py-2 text-black/50 dark:text-white/50">Sin eventos en el rango seleccionado</td></tr>
            )}
          </tbody>
        </table>
      </section>
    </AppShell>
  );
}
