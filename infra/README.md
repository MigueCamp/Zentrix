# Infra — Despliegue y CI/CD

Contiene todo lo necesario para construir, desplegar y operar la plataforma Zentrix (backend, frontend y servicios asociados) de forma reproducible.

## Responsabilidades

- Definir los entornos de contenedores para `backend` (Spring Boot) y `frontend` (Next.js).
- Orquestar servicios locales de desarrollo (SQL Server, backend, frontend) vía Docker Compose.
- Automatizar build, test y despliegue mediante pipelines CI/CD.
- Centralizar variables de entorno y configuración por ambiente (dev, staging, producción).

## Estructura prevista

```
infra/
├── docker/
│   ├── backend.Dockerfile
│   ├── frontend.Dockerfile
│   └── docker-compose.yml     # backend + frontend + SQL Server para desarrollo local
├── ci/
│   └── pipelines/               # Definiciones de CI/CD (build, test, deploy)
└── env/
    ├── .env.example
    └── ...
```

## Estado actual (endurecimiento)

- `backend.Dockerfile`, `frontend.Dockerfile` y `docker-compose.yml` existen y están validados: levantan SQL Server + backend + frontend en local (`sqlserver-init` crea la base `zentrix` antes de que el backend arranque).
- **Pipeline CI/CD implementado** en [`.github/workflows/ci.yml`](../.github/workflows/ci.yml) con las etapas del doc 06 (build, test, lint, build de imágenes a GHCR, deploy a staging/producción). Ver [`ci/pipelines/README.md`](ci/pipelines/README.md) para el mapeo etapa↔job y la configuración pendiente de los ambientes reales.
- **Perfiles por ambiente listos**: `application-staging.yml` y `application-prod.yml` (en `backend/src/main/resources/`) solo referencian variables de entorno, sin secretos embebidos — TLS obligatorio hacia SQL Server (`trustServerCertificate=false`), logs estructurados en JSON/ECS (docs/06 §6) y, en producción, Flyway **deshabilitado** al arranque para ejecutar las migraciones como paso controlado del pipeline (docs/06 §5). Ambos perfiles fueron arrancados y verificados contra SQL Server real.
- **Plantilla de variables** en [`env/.env.example`](env/.env.example): copiar como `.env` por ambiente y completar con valores reales (nunca versionar el `.env` real).
- **Pruebas de carga** en [`load/`](load/README.md): harness de heartbeat de la flota (Python + k6) para validar que la API y la Cola de Comandos no degraden bajo carga (docs/07 §5).
- Pendiente para un despliegue productivo real: crear los *environments* `staging`/`production` en GitHub con sus secretos y conectar el destino de deploy en `ci.yml`.
