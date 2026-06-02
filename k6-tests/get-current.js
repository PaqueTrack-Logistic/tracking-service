/**
 * TEST 3: GET /{shipmentId}/current — Estado actual del envío
 *
 * Estrategia:
 *  - El endpoint más liviano de los tres: solo retorna el status actual
 *  - Ideal para un breakpoint agresivo (arrival-rate)
 *  - Simula clientes consultando estado en tiempo real (alta frecuencia)
 */

import http  from "k6/http";
import { check, sleep } from "k6";
import { Trend, Rate } from "k6/metrics";
import { BASE_URL, THRESHOLDS, JSON_HEADERS } from "./config.js";
import { SHIPMENTS } from "./shipments.js";

// ── Métricas custom ──────────────────────────────────────────────────────────
const currentLatency = new Trend("current_latency_ms", true);
const errorRate      = new Rate("current_error_rate");

// ── Opciones del test ────────────────────────────────────────────────────────
export const options = {
  thresholds: {
    ...THRESHOLDS,
    current_latency_ms: ["p(95)<200", "p(99)<400"],  // debe ser el más rápido
    current_error_rate: ["rate<0.01"],
  },

  scenarios: {
    // Escenario 1: Alto throughput constante (arrival-rate = req/seg fijos)
    high_throughput: {
      executor:         "constant-arrival-rate",
      rate:             80,             // 80 req/seg
      timeUnit:         "1s",
      duration:         "2m",
      preAllocatedVUs:  40,
      maxVUs:           120,
      startTime:        "0s",
      tags:             { scenario: "throughput" },
    },

    // Escenario 2: Rampa de throughput — encuentra el límite
    throughput_ramp: {
      executor:         "ramping-arrival-rate",
      startRate:        20,
      timeUnit:         "1s",
      preAllocatedVUs:  50,
      maxVUs:           300,
      stages: [
        { duration: "1m", target: 100 },
        { duration: "1m", target: 200 },
        { duration: "1m", target: 350 },
      ],
      startTime:        "2m30s",
      tags:             { scenario: "ramp_throughput" },
    },

    // Escenario 3: Punto de quiebre absoluto
    breakpoint: {
      executor:         "ramping-arrival-rate",
      startRate:        50,
      timeUnit:         "1s",
      preAllocatedVUs:  100,
      maxVUs:           500,
      stages: [
        { duration: "2m", target: 300 },
        { duration: "2m", target: 500 },
        { duration: "1m", target: 700 },
      ],
      startTime:        "6m",
      tags:             { scenario: "breakpoint" },
    },
  },
};

// ── Función principal ────────────────────────────────────────────────────────
export default function () {

  const shipmentId = SHIPMENTS[__VU % SHIPMENTS.length];
  const res = http.get(
    `${BASE_URL}/${shipmentId}/current`,
    { headers: JSON_HEADERS }
  );

  currentLatency.add(res.timings.duration);

  const ok = check(res, {
    "status 200":        (r) => r.status === 200,
    "tiene 'status'":    (r) => {
      try { return JSON.parse(r.body).status !== undefined; }
      catch { return false; }
    },
    "latencia < 200ms":  (r) => r.timings.duration < 200,
  });

  errorRate.add(res.status !== 200 ? 1 : 0);

  sleep(0.1);
}