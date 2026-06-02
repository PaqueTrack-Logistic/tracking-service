/**
 * TEST 2: GET /{shipmentId}/history — Historial de eventos paginado
 *
 * Estrategia:
 *  - Endpoint de solo lectura: puede golpearse con más VUs sin conflictos de estado
 *  - Varía página y tamaño para simular uso real
 *  - Spike test: simula pico súbito de consultas (ej. muchos clientes revisando su paquete)
 *
 * IMPORTANTE: Ejecutar seed.sql y register-event.js antes para tener datos en BD.
 */

import http  from "k6/http";
import { check, sleep } from "k6";
import { Trend, Rate, Counter } from "k6/metrics";
import { BASE_URL, THRESHOLDS, JSON_HEADERS } from "./config.js";
import { SHIPMENTS } from "./shipments.js";

// ── Métricas custom ──────────────────────────────────────────────────────────
const historyLatency  = new Trend("history_latency_ms", true);
const emptyResponses  = new Counter("history_empty_pages");
const errorRate       = new Rate("history_error_rate");

// ── Opciones del test ────────────────────────────────────────────────────────
export const options = {
  thresholds: {
    ...THRESHOLDS,
    history_latency_ms: ["p(95)<300", "p(99)<600"],  // GET debe ser más rápido que POST
    history_error_rate: ["rate<0.01"],
  },

  scenarios: {
    // Escenario 1: Carga constante moderada
    sustained_load: {
      executor:  "constant-vus",
      vus:       30,
      duration:  "3m",
      startTime: "0s",
      tags:      { scenario: "sustained" },
    },

    // Escenario 2: Spike súbito — pico de clientes consultando historial
    spike: {
      executor:  "ramping-vus",
      startVUs:  0,
      stages: [
        { duration: "10s", target: 150 },  // sube rápido
        { duration: "45s", target: 150 },  // mantiene el pico
        { duration: "10s", target: 0   },  // baja rápido
      ],
      startTime: "3m30s",
      tags:      { scenario: "spike" },
    },

    // Escenario 3: Carga sostenida alta — ¿aguanta 5 min a 50 VUs?
    high_sustained: {
      executor:  "constant-vus",
      vus:       50,
      duration:  "5m",
      startTime: "5m",
      tags:      { scenario: "high_sustained" },
    },
  },
};

// ── Función principal ────────────────────────────────────────────────────────
export default function () {

  const shipmentId = SHIPMENTS[__VU % SHIPMENTS.length];

  // Varía la página aleatoriamente para evitar caché y simular uso real
  const page = Math.floor(Math.random() * 3);  // páginas 0, 1, 2
  const size = [10, 20, 50][Math.floor(Math.random() * 3)];

  const url = `${BASE_URL}/${shipmentId}/history?page=${page}&size=${size}`;
  const res = http.get(url, { headers: JSON_HEADERS });

  historyLatency.add(res.timings.duration);

  const ok = check(res, {
    "status 200":                      (r) => r.status === 200,
    "body tiene 'content'":            (r) => {
      try { return JSON.parse(r.body).content !== undefined; }
      catch { return false; }
    },
    "body tiene 'totalElements'":      (r) => {
      try { return JSON.parse(r.body).totalElements !== undefined; }
      catch { return false; }
    },
    "latencia < 300ms":                (r) => r.timings.duration < 300,
  });

  if (res.status === 200) {
    errorRate.add(0);
    try {
      const body = JSON.parse(res.body);
      if (body.content && body.content.length === 0) {
        emptyResponses.add(1); // útil para saber si hay datos en BD
      }
    } catch (_) {}
  } else {
    errorRate.add(1);
  }

  sleep(0.5);
}