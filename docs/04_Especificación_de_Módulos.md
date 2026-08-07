# 04. Especificación de Módulos — Zentrix

Este documento detalla el funcionamiento de cada módulo del sistema, mapeado 1:1 al flujo funcional de [01_Visión_del_Proyecto.md](01_Visión_del_Proyecto.md) y a las entidades de [03_Modelo_de_Datos.md](03_Modelo_de_Datos.md). Los endpoints listados son la base del contrato de API (`shared/api-contract`) y del paquete `backend/src/main/java/com/zentrix/*` correspondiente.

## 1. Administración de Empresas (`company`)

**Objetivo:** permitir al Super Administrador dar de alta y mantener las empresas (tenants) del SaaS.

**Funcionalidades**
- **Crear Empresa** — alta con nombre, RUC/NIT y estado inicial `Activa`.
- **Editar Empresa** — actualizar datos y estado (`Activa`/`Suspendida`).
- **Eliminar Empresa** — baja lógica (soft delete); no se eliminan físicamente por trazabilidad.

**Reglas de negocio**
- Solo el rol **Super Administrador** accede a este módulo.
- Una empresa `Suspendida` bloquea el login de todos sus usuarios y la comunicación de sus dispositivos, sin borrar datos.
- El RUC/NIT es único en toda la plataforma.

**Entidades:** `EMPRESA`.

**Endpoints principales:** `POST /companies` · `GET /companies` · `PUT /companies/{id}` · `DELETE /companies/{id}`.

## 2. Administración de Usuarios (`user`)

**Objetivo:** gestionar los usuarios del panel dentro de cada empresa, sus roles, permisos y trazabilidad de acciones.

**Funcionalidades**
- **Crear Usuario** — alta con email único, contraseña (hash), empresa asociada.
- **Asignar Roles** — vincular uno o más roles (`USUARIO_ROL`).
- **Permisos** — definir/editar los permisos que componen cada rol (`ROL_PERMISO`).
- **Auditoría** — consulta de `LOG_AUDITORIA` filtrable por usuario, acción y fecha.

**Reglas de negocio**
- Un Administrador de Empresa solo gestiona usuarios de su propia empresa.
- No se puede eliminar al último usuario con rol de administrador de una empresa.
- Toda creación/edición de usuario, rol o permiso genera un registro en `LOG_AUDITORIA`.

**Entidades:** `USUARIO`, `ROL`, `PERMISO`, `USUARIO_ROL`, `ROL_PERMISO`, `LOG_AUDITORIA`.

**Endpoints principales:** `POST /users` · `GET /users` · `PUT /users/{id}` · `POST /users/{id}/roles` · `GET /roles` · `PUT /roles/{id}/permissions` · `GET /audit-logs`.

## 3. Gestión de Dispositivos (`device`)

**Objetivo:** administrar el ciclo de vida del dispositivo Android dentro de la empresa.

**Funcionalidades**
- **Registrar Dispositivo** — enrollment: el agente Android intercambia un código/token de inscripción por credenciales de dispositivo.
- **Inventario** — listado con filtros (estado, grupo, modelo, versión Android).
- **Estado Online** — indicador en tiempo real basado en el último heartbeat (< 5 min = Online).
- **Agrupar Equipos** — asignar/mover dispositivos entre `GRUPO_DISPOSITIVO`.
- **Historial** — línea de tiempo de eventos (`EVENTO_DISPOSITIVO`) y comandos ejecutados sobre el equipo.

**Reglas de negocio**
- El IMEI es único por dispositivo en toda la plataforma (evita doble inscripción entre empresas).
- Un dispositivo sin heartbeat por más de N minutos configurables pasa automáticamente a `Offline`.
- Eliminar un dispositivo revoca su `TokenEnrollment` de inmediato.

**Entidades:** `DISPOSITIVO`, `GRUPO_DISPOSITIVO`, `EVENTO_DISPOSITIVO`.

**Endpoints principales:** `POST /devices/enroll` · `GET /devices` · `GET /devices/{id}` · `PUT /devices/{id}/group` · `GET /devices/{id}/history` · `DELETE /devices/{id}`.

## 4. Perfiles y Políticas (`policy`)

**Objetivo:** definir configuraciones de seguridad y restricciones, y aplicarlas a dispositivos o grupos.

**Funcionalidades**
- **Crear Perfil** — nueva `POLITICA` con nombre y tipo.
- **Configurar WiFi** — SSID, seguridad, contraseña (cifrada).
- **VPN** — parámetros de conexión VPN corporativa.
- **Modo Kiosco** — restringe el dispositivo a una o varias apps autorizadas.
- **Restricciones** — deshabilitar cámara, USB, instalación de apps de fuentes desconocidas, etc.
- **Asignar Perfil** — vincular la política a un `DISPOSITIVO` o `GRUPO_DISPOSITIVO` (`ASIGNACION_POLITICA`).

**Reglas de negocio**
- Un dispositivo puede tener varias políticas activas de distinto `Tipo`, pero solo una por tipo a la vez (la última asignación reemplaza a la anterior del mismo tipo).
- Cambios en una política se propagan a todos los dispositivos/grupos asignados mediante el flujo de comandos asíncrono (ver `02_Arquitectura_del_Sistema.md`, sección 4.2).
- La configuración sensible (contraseñas WiFi/VPN) se almacena cifrada en `ConfiguracionJson`.

**Entidades:** `POLITICA`, `ASIGNACION_POLITICA`.

**Endpoints principales:** `POST /policies` · `GET /policies` · `PUT /policies/{id}` · `POST /policies/{id}/assign`.

## 5. Aplicaciones (`application`)

**Objetivo:** distribuir y mantener el catálogo de aplicaciones (APK) instalado en la flota.

**Funcionalidades**
- **Subir APK** — carga del archivo al almacenamiento de APKs y registro en `APLICACION`.
- **Instalar** — comando de instalación hacia dispositivo(s) o grupo(s).
- **Actualizar** — nueva versión de una app existente; se distribuye a los dispositivos que ya la tienen instalada.
- **Desinstalar** — comando remoto de desinstalación.
- **Versiones** — historial de versiones publicadas por aplicación.

**Reglas de negocio**
- El `PackageName` identifica la app de forma única dentro de la empresa; subir un APK con el mismo package crea una nueva versión, no una app duplicada.
- Toda instalación/actualización/desinstalación pasa por la Cola de Comandos (asíncrono) y actualiza `DISPOSITIVO_APLICACION`.
- Solo se permite instalar apps del catálogo de la propia empresa.

**Entidades:** `APLICACION`, `DISPOSITIVO_APLICACION`.

**Endpoints principales:** `POST /applications` · `GET /applications` · `POST /applications/{id}/install` · `POST /applications/{id}/uninstall` · `GET /applications/{id}/versions`.

## 6. Monitoreo (`monitoring`)

**Objetivo:** ofrecer visibilidad en tiempo (casi) real del estado de cada dispositivo.

**Funcionalidades**
- **Ubicación** — última coordenada reportada (si el permiso está habilitado en el perfil).
- **Batería** — nivel de carga y estado (cargando/descargando).
- **Almacenamiento** — espacio usado/disponible.
- **Memoria** — uso de RAM.
- **Alertas** — generación y listado de `ALERTA` según reglas configurables (ej. batería < 15%).
- **Última Conexión** — timestamp del último heartbeat exitoso.

**Reglas de negocio**
- Cada heartbeat del agente inserta un registro en `EVENTO_DISPOSITIVO` y actualiza el estado "actual" del dispositivo (proyección de lectura rápida, sin recorrer todo el historial).
- Las reglas de alerta son configurables por empresa (ver módulo Configuración).
- Los eventos se transmiten a la Consola Web vía WebSocket apenas se persisten.

**Entidades:** `EVENTO_DISPOSITIVO`, `ALERTA`, `DISPOSITIVO`.

**Endpoints principales:** `GET /devices/{id}/status` · `GET /devices/{id}/location` · `GET /alerts` · `PUT /alerts/{id}/acknowledge` · WebSocket `/ws/devices`.

## 7. Reportes (`report`)

**Objetivo:** generar reportes exportables sobre el estado y la actividad de la flota.

**Funcionalidades**
- **Inventario** — listado completo de dispositivos con sus atributos y estado.
- **Eventos** — historial de telemetría/comandos filtrable por rango de fechas y tipo.
- **Exportar PDF** — generación de reporte en PDF.
- **Exportar Excel** — generación de reporte en XLSX.

**Reglas de negocio**
- Los reportes se generan siempre acotados al `EmpresaId` del usuario solicitante.
- Reportes de gran volumen (> umbral configurable de filas) se generan de forma asíncrona y quedan disponibles para descarga.

**Entidades:** consultas de solo lectura sobre `DISPOSITIVO`, `EVENTO_DISPOSITIVO`, `LOG_AUDITORIA`.

**Endpoints principales:** `GET /reports/inventory` · `GET /reports/events` · `GET /reports/inventory/export?format=pdf|xlsx`.

## 8. Configuración (`config`)

**Objetivo:** parámetros administrativos de la plataforma y de cada empresa.

**Funcionalidades**
- **Roles** — catálogo de roles disponibles (ver módulo Usuarios para su asignación).
- **Seguridad** — políticas de contraseña, expiración de sesión, doble factor (a futuro).
- **API** — generación y revocación de API keys para integraciones externas.
- **Logs** — acceso centralizado a `LOG_AUDITORIA` y logs técnicos del sistema.
- **Integraciones** — configuración de conectores externos (a definir en fases posteriores).

**Reglas de negocio**
- Las API keys quedan asociadas a una empresa y heredan su nivel de aislamiento (multi-tenant).
- Cambios de configuración de seguridad quedan registrados en auditoría.

**Entidades:** `EMPRESA` (configuración embebida), `LOG_AUDITORIA`, catálogo de `PERMISO`.

**Endpoints principales:** `GET /settings/security` · `PUT /settings/security` · `POST /settings/api-keys` · `DELETE /settings/api-keys/{id}` · `GET /settings/logs`.

## 9. Próximo Documento

- `05_Seguridad_y_Cumplimiento.md` — autenticación, autorización, cifrado, gestión de secretos y buenas prácticas de seguridad transversales a todos los módulos.
