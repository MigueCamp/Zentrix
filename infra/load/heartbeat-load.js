// Prueba de carga del heartbeat con k6 (docs/07 §5) — para ejecución a gran escala en
// infraestructura dedicada. Reutiliza los device tokens aprovisionados por heartbeat_load.py
// (ejecútalo antes con --tokens-out tokens.json).
//
// Ejemplo:
//   python3 heartbeat_load.py --devices 500 --total 1 --tokens-out tokens.json
//   BASE_URL=https://api.staging.example.com TOKENS_FILE=tokens.json VUS=500 \
//     k6 run heartbeat-load.js
import http from 'k6/http';
import { check } from 'k6';
import { SharedArray } from 'k6/data';

const tokens = new SharedArray('device-tokens', () =>
  JSON.parse(open(__ENV.TOKENS_FILE || './tokens.json')));

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const VUS = __ENV.VUS ? parseInt(__ENV.VUS, 10) : 200;

export const options = {
  scenarios: {
    fleet_heartbeat: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: VUS },   // rampa de subida
        { duration: '2m', target: VUS },     // carga sostenida (flota grande)
        { duration: '15s', target: 0 },      // rampa de bajada
      ],
    },
  },
  thresholds: {
    // Criterios de aceptación: <1% de error y p95 por debajo de 1s en infra dedicada.
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1000'],
  },
};

export default function () {
  const token = tokens[Math.floor(Math.random() * tokens.length)];
  const body = JSON.stringify({
    batteryLevel: Math.floor(Math.random() * 95) + 5,
    storageFreeBytes: 5000000000,
    memoryUsedBytes: 1000000000,
    memoryTotalBytes: 4000000000,
    latitude: -12.0 + Math.random(),
    longitude: -77.0 + Math.random(),
  });
  const res = http.post(`${BASE}/devices/heartbeat`, body, {
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
  });
  check(res, { 'heartbeat 2xx': (r) => r.status >= 200 && r.status < 300 });
}
