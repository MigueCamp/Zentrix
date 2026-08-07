export interface NavLink {
  href: string;
  label: string;
}

export const NAV_LINKS: NavLink[] = [
  { href: "/dashboard", label: "Dashboard" },
  { href: "/companies", label: "Empresas" },
  { href: "/users", label: "Usuarios" },
  { href: "/devices", label: "Dispositivos" },
  { href: "/policies", label: "Perfiles y Políticas" },
  { href: "/applications", label: "Aplicaciones" },
  { href: "/monitoring", label: "Monitoreo" },
  { href: "/reports", label: "Reportes" },
  { href: "/settings", label: "Configuración" },
];
