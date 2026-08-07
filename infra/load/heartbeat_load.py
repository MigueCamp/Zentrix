#!/usr/bin/env python3
"""
Prueba de carga del heartbeat de la flota (docs/07 §5).

Aprovisiona N dispositivos reales (registro + enroll) contra el backend Zentrix y luego
dispara TOTAL heartbeats con concurrencia CONCURRENCY, midiendo latencia (p50/p90/p95/p99),
throughput y tasa de error. Usa solo la librería estándar de Python (sin dependencias).

Cada heartbeat persiste varios EVENTO_DISPOSITIVO (conexión/batería/almacenamiento/memoria/
ubicación) y puede generar alertas, así que mide el camino de escritura realista hacia SQL Server.

Uso:
    python3 heartbeat_load.py \
        --base-url http://localhost:8080 \
        --admin-email admin@zentrix.local --admin-password 'Zentrix_Admin_2026!' \
        --devices 50 --total 1000 --concurrency 25 [--tokens-out tokens.json]

Nota: la escala representativa (miles de dispositivos concurrentes) requiere infraestructura
dedicada; este harness también corre a escala modesta en entornos con poca memoria.
"""
import argparse
import concurrent.futures as futures
import json
import random
import time
import urllib.error
import urllib.request
import uuid


def _request(method, url, body=None, token=None, timeout=30):
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(url, data=data, method=method)
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", "Bearer " + token)
    start = time.perf_counter()
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            payload = resp.read().decode() or "null"
            return resp.status, json.loads(payload) if payload.strip() else None, (time.perf_counter() - start)
    except urllib.error.HTTPError as e:
        return e.code, None, (time.perf_counter() - start)


def login(base, email, password):
    status, body, _ = _request("POST", f"{base}/auth/login", {"email": email, "password": password})
    if status != 200:
        raise SystemExit(f"Login falló (HTTP {status}). Revisa credenciales/backend.")
    return body["accessToken"]


def create_company(base, token, suffix):
    _, body, _ = _request("POST", f"{base}/companies",
                          {"name": f"LoadTest-{suffix}", "taxId": f"LT-{suffix}"}, token)
    return body["id"]


def provision_device(base, token, company_id, idx, suffix):
    imei = f"LT-{suffix}-{idx}"
    _, reg, _ = _request("POST", f"{base}/devices?companyId={company_id}",
                        {"imei": imei, "serialNumber": f"SN-{suffix}-{idx}"}, token)
    _, enr, _ = _request("POST", f"{base}/devices/enroll",
                        {"enrollmentToken": reg["enrollmentToken"], "imei": imei,
                         "serialNumber": f"SN-{suffix}-{idx}"})
    return enr["deviceToken"]


def heartbeat(base, device_token):
    body = {
        "batteryLevel": random.randint(5, 100),
        "storageFreeBytes": random.randint(1_000_000_000, 60_000_000_000),
        "memoryUsedBytes": random.randint(500_000_000, 3_000_000_000),
        "memoryTotalBytes": 4_000_000_000,
        "latitude": -12.0 + random.random(),
        "longitude": -77.0 + random.random(),
    }
    status, _, elapsed = _request("POST", f"{base}/devices/heartbeat", body, device_token)
    return status, elapsed


def percentile(sorted_values, p):
    if not sorted_values:
        return 0.0
    k = (len(sorted_values) - 1) * (p / 100.0)
    lo = int(k)
    hi = min(lo + 1, len(sorted_values) - 1)
    return sorted_values[lo] + (sorted_values[hi] - sorted_values[lo]) * (k - lo)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--base-url", default="http://localhost:8080")
    ap.add_argument("--admin-email", default="admin@zentrix.local")
    ap.add_argument("--admin-password", default="Zentrix_Admin_2026!")
    ap.add_argument("--devices", type=int, default=50)
    ap.add_argument("--total", type=int, default=1000)
    ap.add_argument("--concurrency", type=int, default=25)
    ap.add_argument("--tokens-out", default=None, help="Guardar los device tokens (JSON) para reusarlos en k6")
    args = ap.parse_args()

    suffix = uuid.uuid4().hex[:8]
    print(f"→ Login como {args.admin_email}")
    token = login(args.base_url, args.admin_email, args.admin_password)
    company_id = create_company(args.base_url, token, suffix)
    print(f"→ Empresa de carga creada (id={company_id}); aprovisionando {args.devices} dispositivos...")

    device_tokens = []
    with futures.ThreadPoolExecutor(max_workers=args.concurrency) as pool:
        results = pool.map(lambda i: provision_device(args.base_url, token, company_id, i, suffix),
                           range(args.devices))
        device_tokens = list(results)
    print(f"→ {len(device_tokens)} dispositivos inscritos.")

    if args.tokens_out:
        with open(args.tokens_out, "w") as f:
            json.dump(device_tokens, f)
        print(f"→ Tokens guardados en {args.tokens_out}")

    print(f"→ Disparando {args.total} heartbeats (concurrencia {args.concurrency})...")
    latencies = []
    errors = 0
    wall_start = time.perf_counter()
    with futures.ThreadPoolExecutor(max_workers=args.concurrency) as pool:
        jobs = [pool.submit(heartbeat, args.base_url, random.choice(device_tokens))
                for _ in range(args.total)]
        for job in futures.as_completed(jobs):
            status, elapsed = job.result()
            if 200 <= status < 300:  # el heartbeat responde 204 No Content
                latencies.append(elapsed * 1000.0)  # ms
            else:
                errors += 1
    wall = time.perf_counter() - wall_start

    latencies.sort()
    ok = len(latencies)
    print("\n=== Resultados heartbeat ===")
    print(f"Peticiones OK:   {ok}/{args.total}   errores: {errors}")
    print(f"Duración total:  {wall:.2f}s   throughput: {ok / wall:.1f} req/s")
    if latencies:
        print(f"Latencia (ms):   min {latencies[0]:.1f}  p50 {percentile(latencies,50):.1f}  "
              f"p90 {percentile(latencies,90):.1f}  p95 {percentile(latencies,95):.1f}  "
              f"p99 {percentile(latencies,99):.1f}  max {latencies[-1]:.1f}")


if __name__ == "__main__":
    main()
