# 05. Seguridad y Cumplimiento — Zentrix

Este documento define los controles de seguridad transversales a todos los módulos descritos en [04_Especificación_de_Módulos.md](04_Especificación_de_Módulos.md).

## 1. Autenticación

- **Consola Web:** login con email/contraseña; el backend emite un **JWT** de corta duración (access token) + refresh token. `PasswordHash` se almacena con **BCrypt**.
- **Agente Android:** no usa email/contraseña. Se autentica con un `TokenEnrollment` emitido durante el registro (módulo Dispositivos), renovado periódicamente vía un flujo de refresh dedicado.
- **API externa (integraciones):** autenticación por API key (módulo Configuración → API), asociada a una empresa.

## 2. Autorización

- Modelo **RBAC** (Rol–Permiso) definido en `03_Modelo_de_Datos.md` (tablas `ROL`, `PERMISO`, `USUARIO_ROL`, `ROL_PERMISO`).
- Cada endpoint del backend declara los permisos requeridos (ej. `DEVICE_WIPE`, `POLICY_ASSIGN`); el filtro de seguridad de Spring Security valida permiso + tenant en cada request.
- El **Super Administrador** es el único rol con alcance cross-tenant; todo lo demás queda acotado a `EmpresaId` (ver sección 2 de `03_Modelo_de_Datos.md`).

## 3. Aislamiento Multi-Tenant

- Filtro de aplicación (capa `common/`) + **Row-Level Security** en SQL Server, según lo definido en `03_Modelo_de_Datos.md`.
- Pruebas de aislamiento (ver `07_Estrategia_de_Pruebas.md`) verifican explícitamente que un usuario de la Empresa A nunca pueda leer/escribir datos de la Empresa B.

## 4. Cifrado

| Dato | En tránsito | En reposo |
|---|---|---|
| Toda comunicación Consola↔API↔Agente | TLS 1.2+ (HTTPS/WSS) | — |
| Contraseñas de usuario | — | Hash BCrypt (no reversible) |
| Contraseñas WiFi/VPN en políticas | TLS en tránsito | Cifradas en `ConfiguracionJson` (AES-256) con clave gestionada fuera del código fuente |
| Tokens de dispositivo / API keys | TLS en tránsito | Hash o cifrado en base de datos, nunca en texto plano |

## 5. Gestión de Secretos

- Ninguna credencial (DB, claves de cifrado, claves FCM) se almacena en el repositorio ni en `application.yml` versionado.
- Se inyectan por variables de entorno en cada ambiente (ver `06_Despliegue_y_DevOps.md`), gestionadas por el proveedor de secretos del entorno de despliegue (ej. vault del orquestador o secret manager del proveedor cloud).

## 6. Auditoría

- Toda acción administrativa sensible (crear/editar/eliminar empresa, usuario, política, aplicación; cambios de configuración; acciones remotas sobre dispositivos) se registra en `LOG_AUDITORIA` con usuario, acción, detalle y fecha (módulo Configuración → Logs / Usuarios → Auditoría).
- Los logs de auditoría son de solo lectura para todos los roles excepto el proceso de escritura del propio backend (nadie puede editarlos ni borrarlos desde la aplicación).

## 7. Seguridad del Agente Android

- El agente opera como **Device Owner**, lo que le da control administrativo del equipo; por eso su enrollment exige un token de un solo uso con expiración corta.
- Los comandos remotos críticos (`Bloquear`, `Borrar datos`) requieren doble confirmación en la Consola Web antes de encolarse.
- El agente valida la firma/integridad del APK descargado antes de instalar (módulo Aplicaciones) para evitar manipulación en tránsito.

## 8. Protección de la API

- Rate limiting por API key / usuario para mitigar abuso y ataques de fuerza bruta sobre el login.
- Validación estricta de entrada en cada endpoint (evitar inyección SQL — mitigado además por el uso de JPA/Hibernate con consultas parametrizadas — y XSS en campos de texto libre como nombres de política).
- Cabeceras de seguridad estándar (CORS restringido al dominio de la Consola Web, CSP, HSTS) configuradas a nivel de API Gateway/backend.

## 9. Cumplimiento y Retención de Datos

- Los datos de ubicación de un dispositivo solo se recolectan si el perfil/política asignado lo habilita explícitamente (principio de minimización).
- Política de retención configurable por empresa para `EVENTO_DISPOSITIVO` (telemetría histórica) y `LOG_AUDITORIA`, con purga automática tras el período definido.
- Al eliminar una Empresa (baja lógica), sus datos quedan inaccesibles para todos los roles salvo Super Administrador, en preparación para un eventual borrado definitivo bajo solicitud.

## 10. Próximo Documento

- `06_Despliegue_y_DevOps.md` — entornos, pipeline CI/CD, contenedores y variables de configuración por ambiente.
