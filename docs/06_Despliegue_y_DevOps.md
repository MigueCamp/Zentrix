# 06. Despliegue y DevOps — Zentrix

Este documento describe cómo se construyen, configuran y despliegan los componentes definidos en [02_Arquitectura_del_Sistema.md](02_Arquitectura_del_Sistema.md), y se apoya en la estructura ya presente en [infra/](../infra/README.md).

## 1. Ambientes

| Ambiente | Propósito | Datos |
|---|---|---|
| **Local (dev)** | Desarrollo individual, Docker Compose | Datos de prueba, se puede resetear libremente |
| **Staging** | Validación previa a producción, pruebas de integración/QA | Datos sintéticos, espejo de configuración de producción |
| **Producción** | Ambiente real de clientes (empresas) | Datos reales, acceso restringido |

## 2. Contenedores

- `backend/` se empaqueta como imagen Docker a partir de `infra/docker/backend.Dockerfile` (build multi-stage: compilación Maven/Gradle + imagen JRE liviana).
- `frontend/` se empaqueta a partir de `infra/docker/frontend.Dockerfile` (build Next.js + servidor Node en modo producción).
- `infra/docker/docker-compose.yml` levanta en local: `backend`, `frontend`, `SQL Server` y (cuando exista) el broker de la Cola de Comandos, para reproducir el diagrama de componentes completo sin depender de servicios externos.

## 3. Pipeline CI/CD

Definido en `infra/ci/pipelines/`, con las siguientes etapas por cada push/PR:

1. **Build** — compilar backend (Maven/Gradle) y frontend (Next.js).
2. **Test** — ejecutar pruebas unitarias e de integración (ver `07_Estrategia_de_Pruebas.md`).
3. **Análisis estático** — linters (ESLint/TS para frontend, Checkstyle/SpotBugs para backend) y escaneo de dependencias vulnerables.
4. **Build de imágenes** — construir y publicar imágenes Docker versionadas (tag = hash de commit).
5. **Deploy automático a Staging** — en cada merge a `main`.
6. **Deploy a Producción** — manual/aprobado, mismo artefacto ya validado en Staging (no se reconstruye).

## 4. Configuración por Ambiente

- Cada ambiente define sus propias variables de entorno (conexión a SQL Server, claves FCM, secretos de cifrado, URL del broker de mensajería) siguiendo `infra/env/.env.example` como plantilla, nunca con valores reales versionados.
- El backend expone perfiles Spring (`application-dev.yml`, `application-staging.yml`, `application-prod.yml`) que solo referencian variables de entorno, sin secretos embebidos.

## 5. Migraciones de Base de Datos

- Flyway ejecuta las migraciones de forma automática al iniciar el backend en `dev`/`staging`.
- En **producción**, las migraciones se ejecutan como un paso explícito y controlado del pipeline (no automático en el arranque del contenedor), para permitir revisión previa en cambios estructurales sobre SQL Server.

## 6. Observabilidad

- **Logs** centralizados de backend y frontend (salida estructurada JSON), recolectados por el stack de logging del ambiente de despliegue.
- **Métricas** técnicas del backend (latencia, tasa de error, uso de la Cola de Comandos) expuestas para scraping (ej. endpoint `/actuator/metrics` de Spring Boot).
- **Alertas de infraestructura** (distintas de las `ALERTA` de negocio del módulo Monitoreo) para caídas de servicio, saturación de cola o errores 5xx sostenidos.

## 7. Escalabilidad

- `backend` es *stateless* (la sesión vive en el JWT), por lo que se puede escalar horizontalmente detrás de un balanceador.
- La Cola de Comandos permite absorber picos de comandos masivos (ej. aplicar una política a miles de dispositivos) sin saturar la API síncrona.
- El almacenamiento de APKs se sirve idealmente detrás de una CDN/URLs firmadas, para no sobrecargar al backend con transferencias de archivos grandes.

## 8. Próximo Documento

- `07_Estrategia_de_Pruebas.md` — niveles de prueba (unitarias, integración, aislamiento multi-tenant, end-to-end) y criterios de cobertura.
