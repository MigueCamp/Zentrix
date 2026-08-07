"use client";

import { useState } from "react";
import { AppShell } from "@/components/AppShell";
import { useAssignUserRoles, useCreateUser, useUsers } from "@/features/users/hooks";
import { useAssignRolePermissions, useCreateRole, usePermissions, useRoles } from "@/features/roles/hooks";
import { useAuditLogs } from "@/features/audit/hooks";

export default function UsersPage() {
  const { data: users } = useUsers();
  const { data: roles } = useRoles();
  const { data: permissions } = usePermissions();
  const { data: auditLogs } = useAuditLogs();

  const createUser = useCreateUser();
  const assignUserRoles = useAssignUserRoles();
  const createRole = useCreateRole();
  const assignRolePermissions = useAssignRolePermissions();

  const [userForm, setUserForm] = useState({ name: "", email: "", password: "" });
  const [roleName, setRoleName] = useState("");
  const [selectedRoleId, setSelectedRoleId] = useState<number | null>(null);

  const selectedRole = roles?.find((r) => r.id === selectedRoleId);

  return (
    <AppShell>
      <h1 className="text-lg font-semibold mb-4">Administración de Usuarios</h1>

      <section className="mb-8">
        <h2 className="font-medium mb-2">Usuarios</h2>
        <form
          className="flex flex-wrap gap-2 mb-3"
          onSubmit={async (e) => {
            e.preventDefault();
            await createUser.mutateAsync(userForm);
            setUserForm({ name: "", email: "", password: "" });
          }}
        >
          <input placeholder="Nombre" required value={userForm.name}
            onChange={(e) => setUserForm({ ...userForm, name: e.target.value })}
            className="rounded border border-black/10 dark:border-white/20 px-3 py-2 text-sm bg-transparent" />
          <input placeholder="Email" type="email" required value={userForm.email}
            onChange={(e) => setUserForm({ ...userForm, email: e.target.value })}
            className="rounded border border-black/10 dark:border-white/20 px-3 py-2 text-sm bg-transparent" />
          <input placeholder="Contraseña" type="password" required value={userForm.password}
            onChange={(e) => setUserForm({ ...userForm, password: e.target.value })}
            className="rounded border border-black/10 dark:border-white/20 px-3 py-2 text-sm bg-transparent" />
          <button type="submit" disabled={createUser.isPending}
            className="rounded bg-black text-white dark:bg-white dark:text-black px-4 py-2 text-sm font-medium disabled:opacity-50">
            Crear usuario
          </button>
        </form>

        <table className="w-full text-sm">
          <thead>
            <tr className="text-left border-b border-black/10 dark:border-white/10">
              <th className="py-2">Nombre</th><th>Email</th><th>Roles</th><th>Asignar rol</th>
            </tr>
          </thead>
          <tbody>
            {users?.map((user) => (
              <tr key={user.id} className="border-b border-black/5 dark:border-white/5">
                <td className="py-2">{user.name}</td>
                <td>{user.email}</td>
                <td>{user.roles.join(", ") || "—"}</td>
                <td>
                  <select
                    defaultValue=""
                    onChange={(e) => {
                      const roleId = Number(e.target.value);
                      if (roleId) assignUserRoles.mutate({ userId: user.id, roleIds: [roleId] });
                    }}
                    className="rounded border border-black/10 dark:border-white/20 bg-transparent text-sm px-2 py-1"
                  >
                    <option value="">Seleccionar…</option>
                    {roles?.map((r) => <option key={r.id} value={r.id}>{r.name}</option>)}
                  </select>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="mb-8">
        <h2 className="font-medium mb-2">Roles y Permisos</h2>
        <form
          className="flex gap-2 mb-3"
          onSubmit={async (e) => {
            e.preventDefault();
            await createRole.mutateAsync({ name: roleName });
            setRoleName("");
          }}
        >
          <input placeholder="Nombre del rol" required value={roleName}
            onChange={(e) => setRoleName(e.target.value)}
            className="rounded border border-black/10 dark:border-white/20 px-3 py-2 text-sm bg-transparent" />
          <button type="submit" disabled={createRole.isPending}
            className="rounded bg-black text-white dark:bg-white dark:text-black px-4 py-2 text-sm font-medium disabled:opacity-50">
            Crear rol
          </button>
        </form>

        <div className="flex gap-6">
          <ul className="text-sm space-y-1 w-48">
            {roles?.map((r) => (
              <li key={r.id}>
                <button
                  onClick={() => setSelectedRoleId(r.id)}
                  className={`text-left w-full px-2 py-1 rounded ${selectedRoleId === r.id ? "bg-black/10 dark:bg-white/10" : ""}`}
                >
                  {r.name} ({r.permissions.length})
                </button>
              </li>
            ))}
          </ul>

          {selectedRole && (
            <div className="text-sm space-y-1">
              <p className="text-black/60 dark:text-white/60 mb-1">Permisos de &quot;{selectedRole.name}&quot;</p>
              {permissions?.map((p) => {
                const checked = selectedRole.permissions.some((sp) => sp.id === p.id);
                return (
                  <label key={p.id} className="flex items-center gap-2">
                    <input
                      type="checkbox"
                      checked={checked}
                      onChange={(e) => {
                        const current = selectedRole.permissions.map((sp) => sp.id);
                        const next = e.target.checked
                          ? [...current, p.id]
                          : current.filter((id) => id !== p.id);
                        assignRolePermissions.mutate({ roleId: selectedRole.id, permissionIds: next });
                      }}
                    />
                    {p.code}
                  </label>
                );
              })}
            </div>
          )}
        </div>
      </section>

      <section>
        <h2 className="font-medium mb-2">Auditoría</h2>
        <table className="w-full text-sm">
          <thead>
            <tr className="text-left border-b border-black/10 dark:border-white/10">
              <th className="py-2">Fecha</th><th>Usuario</th><th>Acción</th><th>Detalle</th>
            </tr>
          </thead>
          <tbody>
            {auditLogs?.content.map((log) => (
              <tr key={log.id} className="border-b border-black/5 dark:border-white/5">
                <td className="py-2">{new Date(log.actionDate).toLocaleString()}</td>
                <td>{log.userEmail}</td>
                <td>{log.action}</td>
                <td className="text-black/60 dark:text-white/60">{log.detailJson}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </AppShell>
  );
}
