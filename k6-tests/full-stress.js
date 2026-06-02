/**
 * SUITE COMPLETA: Prueba los 3 endpoints en paralelo con carga mixta realista
 *
 * Genera reporte HTML al finalizar: stress-report.html
 *
 * Ejecutar:
 *   k6 run k6-tests/full-stress.js
 */

import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";
import { textSummary } from "https://jslib.k6.io/k6-summary/0.0.1/index.js";
import http  from "k6/http";
import { check, sleep, group } from "k6";
import { Trend, Rate, Counter } from "k6/metrics";
import { BASE_URL, THRESHOLDS, JSON_HEADERS } from "./config.js";
import { SHIPMENTS } from "./shipments.js";

// ── Métricas custom por endpoint ─────────────────────────────────────────────
const postLatency     = new Trend("post_event_latency_ms", true);
const getHistLatency  = new Trend("get_history_latency_ms", true);
const getCurrLatency  = new Trend("get_current_latency_ms", true);
const globalErrors    = new Rate("global_error_rate");
const totalAccepted   = new Counter("total_events_accepted");

// ── Opciones del test ────────────────────────────────────────────────────────
export const options = {
  thresholds: {
    ...THRESHOLDS,
    post_event_latency_ms:    ["p(95)<500"],
    get_history_latency_ms:   ["p(95)<300"],
    get_current_latency_ms:   ["p(95)<200"],
    global_error_rate:        ["rate<0.02"],
  },

  scenarios: {
    // Fase 1: calentamiento
    warmup: {
      executor:  "constant-vus",
      vus:       5,
      duration:  "1m",
      startTime: "0s",
      tags:      { fase: "warmup" },
    },
    // Fase 2: carga normal de producción
    normal_load: {
      executor:  "constant-vus",
      vus:       20,
      duration:  "3m",
      startTime: "1m30s",
      tags:      { fase: "normal" },
    },
    // Fase 3: pico de carga
    peak_load: {
      executor:  "ramping-vus",
      startVUs:  20,
      stages: [
        { duration: "1m",  target: 80  },
        { duration: "2m",  target: 80  },
        { duration: "30s", target: 20  },
      ],
      startTime: "5m",
      tags:      { fase: "peak" },
    },
    // Fase 4: punto de quiebre
    breakpoint: {
      executor:         "ramping-arrival-rate",
      startRate:        20,
      timeUnit:         "1s",
      preAllocatedVUs:  50,
      maxVUs:           250,
      stages: [
        { duration: "2m", target: 100 },
        { duration: "2m", target: 200 },
        { duration: "1m", target: 300 },
      ],
      startTime: "9m",
      tags:      { fase: "breakpoint" },
    },
  },
};

// ── Secuencia válida de eventos ───────────────────────────────────────────────
// Estado inicial: CREATED (garantizado por seed.sql)
// Cada VU ejecuta un evento de la secuencia según su iteración
const EVENT_SEQUENCE = [
  "DISPATCHED",           // CREATED          → IN_TRANSIT
  "ARRIVED_AT_HUB",       // IN_TRANSIT        → AT_TRANSIT_POINT
  "DEPARTED_FROM_HUB",    // AT_TRANSIT_POINT  → IN_TRANSIT
  "OUT_FOR_DELIVERY",     // IN_TRANSIT        → OUT_FOR_DELIVERY
  "DELIVERED",            // OUT_FOR_DELIVERY  → DELIVERED
];

function buildPayload(eventType) {
  return JSON.stringify({
    eventType,
    location:   "Bogotá - Centro Logístico",
    occurredAt: new Date(Date.now() - 3000).toISOString().slice(0, 19),
  });
}

// ── Función principal ────────────────────────────────────────────────────────
export default function () {

  const shipmentId = SHIPMENTS[__VU % SHIPMENTS.length];

  // ── GROUP 1: POST evento (1 por iteración, rotando por la secuencia) ───────
  group("POST /events", () => {
    const eventType = EVENT_SEQUENCE[__ITER % EVENT_SEQUENCE.length];
    const res = http.post(
      `${BASE_URL}/${shipmentId}/events`,
      buildPayload(eventType),
      { headers: JSON_HEADERS }
    );

    postLatency.add(res.timings.duration);
    const accepted = res.status === 202;

    check(res, {
      "POST 202 accepted":              () => accepted,
      "POST correlation-id presente":   (r) => r.headers["X-Correlation-Id"] !== undefined,
    });

    if (accepted) totalAccepted.add(1);
    globalErrors.add(accepted ? 0 : 1);
    sleep(0.5);
  });

  // ── GROUP 2: GET historial ─────────────────────────────────────────────────
  group("GET /history", () => {
    const page = Math.floor(Math.random() * 3);
    const res  = http.get(
      `${BASE_URL}/${shipmentId}/history?page=${page}&size=20`,
      { headers: JSON_HEADERS }
    );

    getHistLatency.add(res.timings.duration);

    check(res, {
      "GET history 200":          (r) => r.status === 200,
      "GET history tiene content": (r) => {
        try { return JSON.parse(r.body).content !== undefined; }
        catch { return false; }
      },
    });

    globalErrors.add(res.status !== 200 ? 1 : 0);
    sleep(0.3);
  });

  // ── GROUP 3: GET estado actual ─────────────────────────────────────────────
  group("GET /current", () => {
    const res = http.get(
      `${BASE_URL}/${shipmentId}/current`,
      { headers: JSON_HEADERS }
    );

    getCurrLatency.add(res.timings.duration);

    check(res, {
      "GET current 200":        (r) => r.status === 200,
      "GET current tiene status": (r) => {
        try { return JSON.parse(r.body).status !== undefined; }
        catch { return false; }
      },
    });

    globalErrors.add(res.status !== 200 ? 1 : 0);
    sleep(0.2);
  });
}

// ── Reporte al finalizar ──────────────────────────────────────────────────────
export function handleSummary(data) {
  return {
    "/k6-tests/stress-report.html": htmlReport(data),
    stdout: textSummary(data, { indent: "  ", enableColors: true }),
  };
}
