# 08. Plan de Implementación — Zentrix

Roadmap de construcción del sistema, ordenado para entregar valor incremental y validar primero los riesgos más altos del proyecto (multi-tenancy, comunicación con el dispositivo Android).

## 1. Principio de Orden

1. Primero lo que **todo lo demás depende**: autenticación, multi-tenancy y el esqueleto de comunicación con el dispositivo.
2. Luego los módulos de **datos maestros**: empresas, usuarios, dispositivos.
3. Después la **capacidad de control**: políticas y aplicaciones (lo que justifica el producto como MDM).
4. Por último, **visibilidad y operación**: monitoreo, reportes, configuración avanzada.

## 2. Fase 0 — Fundaciones

- Inicialización de los tres proyectos: `backend` (Spring Boot), `frontend` (Next.js), `device-agent` (Android/Kotlin).
- Conexión backend ↔ SQL Server + primera migración (tabla `EMPRESA`, `USUARIO`).
- Autenticación básica (login + JWT) y aislamiento multi-tenant a nivel de aplicación (filtro por `EmpresaId`).
- Pipeline CI mínimo (build + test) según `06_Despliegue_y_DevOps.md`.

**Criterio de salida:** un Super Administrador puede loguearse y un usuario de una empresa no puede ver datos de otra (primer test de aislamiento en verde).

## 3. Fase 1 — MVP: Empresas, Usuarios y Dispositivos

- Módulo **Administración de Empresas** completo (Crear/Editar/Eliminar).
- Módulo **Administración de Usuarios** completo (roles, permisos, auditoría básica).
- Módulo **Gestión de Dispositivos**: enrollment, inventario, estado online, historial básico.
- `device-agent`: flujo de enrollment contra el backend + heartbeat inicial (sin políticas todavía).
- Row-Level Security en SQL Server activado para las tablas ya existentes.

**Criterio de salida:** un dispositivo Android real puede inscribirse, aparecer en el inventario de su empresa y reportar "online".

## 4. Fase 2 — Control: Políticas y Aplicaciones

- Módulo **Perfiles y Políticas**: creación, configuración (WiFi, VPN, kiosco, restricciones) y asignación a dispositivo/grupo.
- Flujo de comandos asíncrono completo: Cola de Comandos + Push (FCM) + ejecución en el agente.
- Módulo **Aplicaciones**: subir APK, instalar, actualizar, desinstalar, versiones.
- Almacenamiento de APKs integrado (`infra`).

**Criterio de salida:** una política asignada desde la consola se aplica en el dispositivo real en minutos; una app se puede distribuir e instalar remotamente.

## 5. Fase 3 — Visibilidad: Monitoreo y Reportes

- Módulo **Monitoreo**: ubicación, batería, almacenamiento, memoria, alertas, última conexión, WebSocket en tiempo real.
- Módulo **Reportes**: inventario, eventos, exportación PDF/Excel.
- Reglas de alerta configurables por empresa.

**Criterio de salida:** el dashboard refleja el estado de la flota en tiempo real y se puede exportar un reporte de inventario.

## 6. Fase 4 — Configuración Avanzada y Endurecimiento

- Módulo **Configuración**: seguridad (expiración de sesión, políticas de contraseña), API keys, logs centralizados.
- Cifrado de configuración sensible (WiFi/VPN) según `05_Seguridad_y_Cumplimiento.md`.
- Pruebas de carga y resiliencia (`07_Estrategia_de_Pruebas.md`, sección 5).
- Preparación de ambiente de Producción (Fase de `06_Despliegue_y_DevOps.md`).

**Criterio de salida:** el sistema cumple los controles de seguridad definidos y está validado para operar con clientes reales.

## 7. Fuera de Alcance del Roadmap Inicial

Alineado con `01_Visión_del_Proyecto.md`, sección 3.2: soporte iOS, app móvil nativa de administración, facturación/billing automatizado e integraciones específicas con terceros. Se evalúan como fases futuras una vez estabilizado lo anterior.

## 8. Estado de la Documentación

Con este documento se completa la fase de planificación funcional/técnica:

| Doc | Contenido |
|---|---|
| 01 | Visión, objetivos y alcance |
| 02 | Arquitectura del sistema |
| 03 | Modelo de datos |
| 04 | Especificación de módulos |
| 05 | Seguridad y cumplimiento |
| 06 | Despliegue y DevOps |
| 07 | Estrategia de pruebas |
| 08 | Plan de implementación (este documento) |

El siguiente paso natural ya no es documentación, sino **iniciar la Fase 0**: crear los proyectos base (`backend`, `frontend`, `device-agent`) con su configuración inicial.
