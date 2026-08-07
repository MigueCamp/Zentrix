# Backend — Zentrix API

Servicio central de la plataforma **Zentrix** (MDM SaaS). Implementado en **Spring Boot (Java) + SQL Server**, expone la API REST consumida por el frontend (Next.js) y por el agente Android (`device-agent`), y concentra toda la lógica de negocio **multi-tenant**.

## Responsabilidades

- Autenticación y autorización (JWT, roles y permisos por empresa).
- Aislamiento de datos entre empresas (tenants).
- Exponer los endpoints REST de cada módulo funcional.
- Comunicación con los dispositivos Android: envío de comandos remotos y recepción de telemetría (batería, ubicación, estado).
- Persistencia en SQL Server vía JPA/Hibernate, con migraciones versionadas.

## Estructura prevista

```
backend/
├── src/main/java/com/zentrix/
│   ├── auth/           # Login, JWT, control de acceso
│   ├── company/        # Módulo "Administración de Empresas"
│   ├── user/            # Módulo "Administración de Usuarios" (roles, permisos, auditoría)
│   ├── device/          # Módulo "Gestión de Dispositivos" (registro, inventario, estado, grupos, historial)
│   ├── policy/          # Módulo "Perfiles y Políticas" (WiFi, VPN, kiosco, restricciones)
│   ├── application/    # Módulo "Aplicaciones" (subida, instalación, actualización, versiones)
│   ├── monitoring/     # Módulo "Monitoreo" (ubicación, batería, almacenamiento, memoria, alertas)
│   ├── report/          # Módulo "Reportes" (inventario, eventos, exportación PDF/Excel)
│   ├── config/          # Módulo "Configuración" (roles, seguridad, API, logs, integraciones)
│   └── common/          # Utilidades, excepciones y configuración transversal
├── src/main/resources/  # application.yml, migraciones (Flyway, compatible con SQL Server)
└── src/test/             # Pruebas unitarias e integración
```

Cada subpaquete de `com.zentrix.*` corresponde 1 a 1 con un nodo del flujo funcional descrito en [docs/01_Visión_del_Proyecto.md](../docs/01_Visión_del_Proyecto.md).

## Estado actual (Fase 4 — funcionalidad completa)

- Proyecto Spring Boot 4.0.7 (Java 21, Maven).
- `common`: JWT (usuarios y dispositivos), seguridad, `TenantResolver`/`TenantContext` (aislamiento multi-tenant), `PolicyCipher` (AES-256-GCM para configuración sensible), `ApiKeyAuthenticationFilter` (autenticación de integraciones por header `X-API-Key`), WebSocket (`ws/`: handshake autenticado por JWT en query param + difusión de eventos por empresa), manejo de errores.
- `company`: CRUD completo.
- `user`: usuarios, roles, permisos (catálogo fijo), asignación rol↔usuario y rol↔permiso, auditoría (`LOG_AUDITORIA`).
- `device`: registro (pre-enrollment), enroll del agente Android, heartbeat (batería/almacenamiento/memoria/ubicación, cada uno como `EVENTO_DISPOSITIVO` separado), historial de eventos, grupos de dispositivos.
- `policy`: perfiles WiFi/VPN/Kiosco/Restricciones (`POLITICA`), asignación a dispositivo o grupo (`ASIGNACION_POLITICA`, una asignación activa por `Tipo`), configuración WiFi/VPN cifrada en reposo, propagación automática al modificar una política ya asignada.
- `command`: Cola de Comandos (`COMANDO`) compartida por `policy` y `application` — encolado desde el backend, entrega vía polling del agente (`GET /devices/commands/pending`), confirmación de resultado (`POST /devices/commands/{id}/ack`) e historial por dispositivo (`GET /devices/{id}/commands`).
- `application`: catálogo de APKs (`APLICACION`) con subida de archivo a almacenamiento local (`zentrix.storage.apk-dir`), instalar/desinstalar hacia dispositivo o grupo vía Cola de Comandos, estado de instalación por dispositivo (`DISPOSITIVO_APLICACION`), descarga del APK (`GET /applications/{id}/apk`, también usado por el agente).
- `monitoring`: proyección de estado por dispositivo sin recorrer historial (`GET /devices/{id}/status`, `GET /devices/{id}/location`), alertas (`ALERTA`) generadas automáticamente en cada heartbeat (batería < 15%, sin duplicar mientras haya una sin atender del mismo tipo), `GET /alerts` + `PUT /alerts/{id}/acknowledge`, `DeviceOfflineScheduler` (job cada minuto que pasa a `OFFLINE` los dispositivos sin heartbeat en 20 min), y WebSocket `GET /ws/devices?token=<jwt>` que difunde cada heartbeat/cambio de estado solo a las sesiones de la misma empresa.
- `report`: `GET /reports/inventory`, `GET /reports/events` (filtrable por rango de fecha y tipo), `GET /reports/inventory/export?format=pdf|xlsx` (PDFBox / Apache POI, genera archivos reales descargables). Las tres rutas también aceptan autenticación por `X-API-Key` (rol `API_CLIENT`).
- `config`: módulo "Configuración" — seguridad por empresa (`CONFIGURACION_SEGURIDAD`: política de contraseñas y expiración de sesión, `GET`/`PUT /settings/security`), API keys (`API_KEY`, guardadas solo como hash SHA-256, valor en claro mostrado una vez, `GET`/`POST`/`DELETE /settings/api-keys`), y acceso centralizado a auditoría (`GET /settings/logs`). La política de contraseñas se aplica al crear usuarios y la expiración de sesión al firmar cada JWT.
- Migraciones: `V1` (`EMPRESA`/`USUARIO`), `V2` (`ROL`/`PERMISO`/`USUARIO_ROL`/`ROL_PERMISO`/`LOG_AUDITORIA`), `V3` (`GRUPO_DISPOSITIVO`/`DISPOSITIVO`/`EVENTO_DISPOSITIVO`), `V4` (`POLITICA`/`ASIGNACION_POLITICA`), `V5` (`COMANDO`), `V6` (`APLICACION`/`DISPOSITIVO_APLICACION`), `V7` (`ALERTA`), `V8` (`CONFIGURACION_SEGURIDAD`/`API_KEY`).
- Validado de punta a punta contra SQL Server real: login, CRUD, asignación de roles, enrollment de dispositivo simulado, heartbeat con batería/memoria/ubicación, generación y deduplicación de alertas, estado/ubicación por dispositivo, exportación real de PDF/XLSX verificada byte a byte, WebSocket autenticado con broadcast real disparado por un heartbeat y rechazo sin token, política de contraseñas rechazando (400) y aceptando (201) según la config de la empresa, expiración de sesión reflejada en el TTL del JWT (60 min configurados vs 30 min por defecto del Super Admin), ciclo completo de API keys (crear → usar en `/reports` → acotada fuera de otros endpoints (403) → revocar → rechazada), y aislamiento entre empresas (403/404, config y logs independientes, sin fuga de mensajes WebSocket entre tenants) en todos los módulos.

## Pruebas automatizadas

### Unitarias (`./mvnw test`)

Suite de pruebas unitarias (JUnit 5 + Mockito + AssertJ) sobre las reglas de negocio críticas de `docs/07_Estrategia_de_Pruebas.md`. Son puras (sin contexto Spring ni base de datos), así que corren en segundos y sirven de red de regresión en el pipeline CI:

```bash
./mvnw test
```

| Suite | Qué protege |
|---|---|
| `TenantResolverTest` | **Aislamiento multi-tenant** (docs/07 §3, no negociable): un `EMPRESA_ADMIN` nunca opera sobre otra empresa y un `SUPER_ADMIN` debe declarar el tenant explícitamente. |
| `SecuritySettingsServiceTest` | Política de contraseñas por empresa: longitud mínima, mayúscula, dígito y carácter especial, contra la config almacenada o los valores por defecto. |
| `ApiKeyServiceTest` | API keys: la clave en claro se muestra una sola vez y solo se persiste su hash SHA-256 determinista; `resolveCompanyId` reencuentra la empresa por ese hash; `revoke` la desactiva. |
| `PolicyCipherTest` | Cifrado en reposo WiFi/VPN (AES-256-GCM): ida y vuelta, no determinismo (IV aleatorio) y detección de manipulación. |
| `PolicyServiceTest` | Reglas de asignación: exactamente un destino (dispositivo XOR grupo) y "una sola política activa por Tipo" (reemplaza la previa y encola `APPLY_POLICY`). |
| `CommandServiceTest` | Cola de Comandos: encolado `PENDIENTE`, entrega por polling (`PENDIENTE → ENVIADO`) y que un dispositivo no pueda confirmar (`ack`) el comando de otro. |

### Integración (`./mvnw verify`)

Pruebas de integración (`*IT`, ejecutadas por el plugin failsafe en la fase `verify`) que arrancan el contexto Spring completo y ejercitan la **API real por HTTP** (`RestTestClient`) contra un **SQL Server real** con Flyway aplicando el esquema:

| Suite | Qué protege |
|---|---|
| `MultiTenantIsolationIT` | **Aislamiento multi-tenant a nivel HTTP** (docs/07 §3): con JWT reales, un `EMPRESA_ADMIN` de A recibe 404 al leer un dispositivo de B y 403 al operar sobre B. Prueba el cableado completo JWT → `TenantContext` → filtro por `EmpresaId` → código HTTP. |
| `CommandFlowIT` | **Flujo de comandos asíncronos** (docs/07 §4): asignar una política encola un comando, el dispositivo lo recibe por polling (`PENDIENTE → ENVIADO`), lo confirma (`ack`) y no se le re-entrega (entrega idempotente). |

La fuente de datos se resuelve en `AbstractIntegrationTest`:

- **CI / por defecto:** SQL Server efímero vía **Testcontainers** (el runner de GitHub Actions tiene Docker). `./mvnw verify` lo levanta solo.
- **Local con poca memoria:** apuntar a un SQL Server ya corriendo sin levantar otro contenedor:

```bash
# Crear una BD de pruebas aparte en el SQL Server del docker compose:
docker exec docker-sqlserver-1 /opt/mssql-tools18/bin/sqlcmd -C -S localhost -U sa \
  -P 'Zentrix_Dev_2026!' -Q "IF DB_ID('zentrix_test') IS NULL CREATE DATABASE zentrix_test"

IT_DATASOURCE_URL="jdbc:sqlserver://localhost:1433;databaseName=zentrix_test;encrypt=true;trustServerCertificate=true" \
IT_DATASOURCE_USERNAME=sa IT_DATASOURCE_PASSWORD='Zentrix_Dev_2026!' \
  ./mvnw verify
```

### Carga y resiliencia

Prueba del heartbeat de una flota grande (docs/07 §5) en [`infra/load/`](../infra/load/README.md): un harness en Python (sin dependencias) y un script k6 para escala en infraestructura dedicada.

## Cómo levantarlo en local

```bash
cd infra/docker
docker compose up -d --build
```

Esto levanta SQL Server + el backend en `http://localhost:8080`. Al iniciar por primera vez se crea automáticamente un usuario `SUPER_ADMIN` de arranque (`admin@zentrix.local` / `Zentrix_Admin_2026!`, definidos en `application-dev.yml` — solo para desarrollo local).

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@zentrix.local","password":"Zentrix_Admin_2026!"}'
```

El token devuelto se usa como `Authorization: Bearer <token>` para el resto de endpoints (ej. `GET /companies`).

## Perfiles por ambiente

El perfil activo se elige con `SPRING_PROFILES_ACTIVE` (por defecto `dev`):

| Perfil | Datasource | Flyway al arrancar | Logs | Secretos |
|---|---|---|---|---|
| `dev` | SQL Server local, `trustServerCertificate=true`, con defaults de desarrollo | Sí | Texto legible | Defaults locales embebidos (solo dev) |
| `staging` | Env vars, TLS real (`trustServerCertificate=false`) | Sí (automático) | JSON/ECS estructurado | Solo desde variables de entorno |
| `prod` | Env vars, TLS real | **No** (migración = paso controlado del pipeline, docs/06 §5) | JSON/ECS estructurado | Solo desde variables de entorno |

Las variables requeridas por `staging`/`prod` están documentadas en [`infra/env/.env.example`](../infra/env/.env.example). En `prod` el esquema debe estar migrado **antes** de desplegar la imagen; `spring.jpa.hibernate.ddl-auto=validate` verifica al arrancar que el esquema coincida con las entidades.

Los endpoints de `user` y `device` son por empresa: un `EMPRESA_ADMIN` opera siempre sobre la suya; un `SUPER_ADMIN` (sin empresa propia) debe indicar `?companyId=` explícitamente (ver `TenantResolver`) — por ejemplo, para crear el primer usuario administrador de una empresa recién creada.
