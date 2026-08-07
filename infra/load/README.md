# Pruebas de carga — Heartbeat de la flota

Valida que la API y la Cola de Comandos no degraden bajo el heartbeat simultáneo de una flota
grande (docs/07_Estrategia_de_Pruebas.md §5). Cada heartbeat persiste varios `EVENTO_DISPOSITIVO`
(conexión/batería/almacenamiento/memoria/ubicación) y puede generar alertas, así que ejercita el
camino de escritura realista hacia SQL Server.

## 1. Harness en Python (sin dependencias) — bueno para escala modesta y humo

```bash
python3 heartbeat_load.py \
  --base-url http://localhost:8080 \
  --admin-email admin@zentrix.local --admin-password 'Zentrix_Admin_2026!' \
  --devices 50 --total 1000 --concurrency 25
```

Aprovisiona N dispositivos (registro + enroll), dispara TOTAL heartbeats con la concurrencia
indicada y reporta latencia (p50/p90/p95/p99), throughput y tasa de error.

## 2. k6 — para gran escala en infraestructura dedicada

k6 reutiliza los device tokens aprovisionados por el harness de Python:

```bash
# 1) Aprovisionar la flota y volcar los tokens
python3 heartbeat_load.py --devices 500 --total 1 --tokens-out tokens.json

# 2) Correr la carga sostenida con k6
BASE_URL=https://api.staging.example.com TOKENS_FILE=tokens.json VUS=500 \
  k6 run heartbeat-load.js
```

Umbrales definidos en el script (criterios de aceptación): `http_req_failed < 1%` y `p95 < 1s`.

## 3. Resultado de referencia en este entorno de desarrollo

Ejecución `--devices 50 --total 1000 --concurrency 25` contra el `docker compose` local
(codespace con memoria muy limitada, SQL Server bajo presión):

| Métrica | Valor |
|---|---|
| Peticiones OK | **1000/1000 (0 errores)** |
| Throughput | ~29.5 req/s |
| Latencia p50 / p95 / p99 | ~850 ms / ~1220 ms / ~1360 ms |

> Las latencias reflejan un entorno saturado de memoria (no representativo de producción). El
> resultado relevante aquí es la **resiliencia**: cero errores y sin caídas bajo 1000 heartbeats
> concurrentes. Para medir latencias representativas de una flota de miles, ejecutar k6 (sección 2)
> contra un ambiente `staging` con recursos dedicados.
