# Frontend — Consola Web Zentrix

Consola de administración SaaS de Zentrix, construida con **React + TypeScript + Next.js**. Es el punto de acceso único para Super Administradores, Administradores de Empresa, Operadores y Auditores.

## Responsabilidades

- Login y manejo de sesión (consumo de JWT emitido por el backend).
- Renderizar el dashboard y navegar hacia cada módulo funcional.
- Consumir la API REST del backend (Spring Boot) para cada operación (CRUD de empresas, usuarios, dispositivos, políticas, aplicaciones, reportes, configuración).
- Visualización en tiempo real del estado de la flota (ubicación, batería, conectividad, alertas).

## Estructura prevista

```
frontend/
├── src/
│   ├── app/              # Rutas Next.js (App Router): login, dashboard, módulos
│   │   ├── (auth)/       # Login / autenticación
│   │   ├── companies/    # Administración de Empresas
│   │   ├── users/        # Administración de Usuarios
│   │   ├── devices/      # Gestión de Dispositivos
│   │   ├── policies/     # Perfiles y Políticas
│   │   ├── applications/ # Aplicaciones
│   │   ├── monitoring/   # Monitoreo
│   │   ├── reports/      # Reportes
│   │   └── settings/     # Configuración
│   ├── components/       # Componentes UI reutilizables (tablas, formularios, gráficos)
│   ├── features/          # Lógica de cada módulo (hooks, tipos, llamadas API específicas)
│   ├── services/          # Cliente HTTP / integración con la API del backend
│   ├── store/              # Estado global (sesión, tenant activo, etc.)
│   └── styles/              # Estilos globales / theming
└── public/                  # Assets estáticos
```

Cada carpeta bajo `src/app/` corresponde a un módulo del diagrama de flujo en [docs/01_Visión_del_Proyecto.md](../docs/01_Visión_del_Proyecto.md) (D → E, F, G, H, I, J, K, L).

## Estado actual (Fase 4 — funcionalidad completa)

- Next.js + TypeScript + Tailwind, TanStack Query (datos remotos) y Zustand (sesión, persistida con `hasHydrated` para evitar el falso redirect a `/login` en una recarga completa antes de que el store termine de leer `localStorage`).
- Los 9 módulos del diagrama de flujo están funcionales con integración real al backend: **Login**, **Empresas**, **Usuarios** (crear, listar, asignar rol; roles y permisos por checkbox; auditoría), **Dispositivos** (grupos, pre-registro con token de enrollment, inventario con estado en vivo cada 15s), **Perfiles y Políticas** (crear perfil WiFi/VPN/Kiosco/Restricciones con formulario específico por tipo, asignar a dispositivo o grupo), **Aplicaciones** (subir APK con metadata, instalar/desinstalar hacia dispositivo o grupo, ver estado de instalación en vivo), **Monitoreo** (batería/almacenamiento/memoria/ubicación por dispositivo, panel de alertas con botón "Atender", feed de actividad en vivo vía `WebSocket` nativo contra `/ws/devices`), **Reportes** (inventario y eventos filtrables por rango de fecha/tipo, descarga real de PDF/XLSX), **Configuración** (política de seguridad editable, API keys con generación —mostrada una sola vez—/listado/revocación, tabla de logs de auditoría).
- Validado en navegador real (Playwright) fase por fase, la última: login como `EMPRESA_ADMIN`, edición de la política de seguridad, generación y revocación de una API key (con visualización única del valor en claro) y tabla de logs — sin errores de consola.

## Cómo levantarlo en local

```bash
cd infra/docker
docker compose up -d --build
```

Consola en `http://localhost:3000` (requiere el backend del mismo `docker compose`, en `http://localhost:8080`).
