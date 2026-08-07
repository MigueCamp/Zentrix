# 07. Estrategia de Pruebas — Zentrix

Define cómo se valida la calidad del sistema en cada capa, con énfasis en los dos riesgos más críticos del proyecto: el **aislamiento multi-tenant** y la **fiabilidad de los comandos remotos** hacia el dispositivo.

## 1. Niveles de Prueba

| Nivel | Alcance | Herramientas sugeridas |
|---|---|---|
| **Unitarias** | Reglas de negocio de cada módulo (`company`, `user`, `device`, `policy`, `application`, `monitoring`, `report`, `config`) de forma aislada | JUnit 5 + Mockito (backend), Jest + Testing Library (frontend) |
| **Integración (backend)** | Endpoints reales contra una SQL Server de prueba (contenedor efímero) | Spring Boot Test + Testcontainers |
| **Integración (frontend)** | Flujos de pantalla contra una API mockeada/stub | Testing Library + MSW |
| **End-to-End (E2E)** | Flujos completos de usuario en la Consola Web (login → acción → resultado visible) | Playwright/Cypress |
| **Aislamiento multi-tenant** | Verificar que ninguna operación cruce datos entre empresas | Suite de integración dedicada (ver sección 3) |
| **Agente Android** | Aplicación de políticas y ejecución de comandos en el dispositivo/emulador | Espresso / UI Automator |

## 2. Cobertura por Módulo

Cada módulo del backend (`04_Especificación_de_Módulos.md`) debe cubrir con pruebas unitarias/integración, como mínimo:

- El **camino feliz** de cada funcionalidad listada (ej. Crear Empresa, Asignar Perfil, Instalar App).
- Las **reglas de negocio** explícitas del documento (ej. "un dispositivo no puede tener dos políticas activas del mismo tipo", "el RUC/NIT es único").
- Los **casos de error** esperados (permiso insuficiente, entidad no encontrada, dato duplicado).

## 3. Pruebas de Aislamiento Multi-Tenant

Suite específica que, para cada endpoint sensible a tenant, verifica:

1. Un usuario de la Empresa A no puede leer un recurso (`GET`) de la Empresa B, aunque conozca su ID.
2. Un usuario de la Empresa A no puede modificar/eliminar un recurso de la Empresa B.
3. Las políticas de **Row-Level Security** en SQL Server bloquean el acceso incluso si el filtro de aplicación fallara (prueba de "doble capa" descrita en `03_Modelo_de_Datos.md`).

Esta suite se ejecuta en cada pipeline (ver `06_Despliegue_y_DevOps.md`) y su fallo bloquea el merge.

## 4. Pruebas del Flujo de Comandos Asíncronos

Para el flujo Cola → Push (FCM) → Dispositivo → API (`02_Arquitectura_del_Sistema.md`, sección 4.2):

- Prueba de integración que simula la publicación de un comando y verifica que quede correctamente encolado y asociado al dispositivo destino.
- Prueba de reintento: si el dispositivo no confirma ejecución en el tiempo esperado, el comando se marca en error/reintento (no queda "colgado" indefinidamente).
- Prueba de idempotencia: reenviar el mismo comando no debe duplicar su efecto (ej. instalar la misma app dos veces).

## 5. Pruebas No Funcionales

- **Carga:** simular el heartbeat simultáneo de una flota grande (miles de dispositivos) para validar que la API y la Cola de Comandos no degraden el tiempo de respuesta de la Consola Web.
- **Seguridad:** pruebas automatizadas de vulnerabilidades comunes (inyección, XSS, control de acceso roto) integradas al pipeline, alineadas con `05_Seguridad_y_Cumplimiento.md`.
- **Resiliencia:** verificar que la caída temporal del broker de mensajería o del servicio Push no provoque pérdida de comandos (deben quedar persistidos y reintentarse).

## 6. Criterios de Aceptación para Merge

- Todas las pruebas unitarias y de integración en verde.
- Cobertura mínima acordada por módulo (a definir con el equipo, ej. 80% en lógica de negocio backend).
- La suite de aislamiento multi-tenant siempre en verde (no negociable).

## 7. Próximo Documento

- `08_Plan_de_Implementación.md` — fases de desarrollo, alcance del MVP y orden sugerido de construcción de los módulos.
