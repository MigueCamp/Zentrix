# Pipelines CI/CD — Zentrix

El pipeline descrito en [docs/06_Despliegue_y_DevOps.md](../../../docs/06_Despliegue_y_DevOps.md) §3 está implementado como un workflow de **GitHub Actions** en:

- [`.github/workflows/ci.yml`](../../../.github/workflows/ci.yml)

Se ejecuta en cada `push` y en cada `pull_request` a `main`, con estas etapas (mismo orden que el doc 06):

| # | Etapa | Job del workflow | Herramienta |
|---|---|---|---|
| 1 | Build | `backend`, `frontend` | Maven (`./mvnw verify`) + Next.js (`npm run build`) |
| 2 | Test | `backend` | JUnit 5 + Mockito (25 pruebas unitarias, ver [backend/README.md](../../../backend/README.md)) |
| 3 | Análisis estático | `frontend` | ESLint (`npm run lint`) |
| 4 | Build de imágenes | `images` | Docker → GHCR, tag = `${{ github.sha }}` (solo en push a `main`) |
| 5 | Deploy a Staging | `deploy-staging` | Automático en merge a `main`, environment `staging` |
| 6 | Deploy a Producción | `deploy-production` | Manual (`workflow_dispatch`), environment `production` |

## Configuración pendiente al conectar infraestructura real

Los jobs de deploy (5 y 6) están como **andamiaje**: publican el tag del artefacto y documentan el paso, pero el comando de despliegue real depende del proveedor del ambiente. Para activarlos:

1. Crear los *environments* `staging` y `production` en GitHub (Settings → Environments), con reglas de aprobación para `production`.
2. Añadir a cada environment los secretos del destino (credenciales del clúster/host, cadenas de conexión a SQL Server, `ZENTRIX_JWT_SECRET`, `ZENTRIX_ENCRYPTION_KEY`, claves FCM) — nunca versionados, según [docs/06 §4](../../../docs/06_Despliegue_y_DevOps.md).
3. Reemplazar los pasos `TODO` de `ci.yml` por la invocación real del deploy.
4. En Producción, las migraciones Flyway se ejecutan como **paso explícito y revisado** del pipeline, no en el arranque del contenedor (docs/06 §5).
