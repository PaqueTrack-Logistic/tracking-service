/**
 * TEST 1: POST /{shipmentId}/events — Registrar evento de tracking
 *
 * Estrategia:
 *  - Cada VU toma un shipmentId diferente del pool (evita conflictos de estado)
 *  - Ejecuta la secuencia de transiciones válidas completa
 *  - Tres escenarios: carga sostenida, rampa, punto de quiebre
 *
 * IMPORTANTE: Ejecutar seed.sql antes de correr este test.
 *
 * Secuencia de transiciones válidas:
 *  CREATED
 *    →[DISPATCHED]→         IN_TRANSIT
 *    →[ARRIVED_AT_HUB]→     AT_TRANSIT_POINT
 *    →[DEPARTED_FROM_HUB]→  IN_TRANSIT
 *    →[OUT_FOR_DELIVERY]→   OUT_FOR_DELIVERY
 *    →[DELIVERED]→          DELIVERED  (estado final)
 */

import http  from "k6/http";
import { check, sleep } from "k6";
import { Counter, Rate, Trend } from "k6/metrics";
import { BASE_URL, THRESHOLDS, JSON_HEADERS } from "./config.js";
import { SHIPMENTS } from "./shipments.js";

// ── Métricas custom ──────────────────────────────────────────────────────────
const transitionErrors   = new Counter("transition_errors");
const acceptedEvents     = new Counter("accepted_events");
const eventLatency       = new Trend("event_latency_ms", true);
const errorRate          = new Rate("event_error_rate");

// ── Opciones del test ────────────────────────────────────────────────────────
export const options = {
  thresholds: {
    ...THRESHOLDS,
    transition_errors: ["count<5"],   // máximo 5 errores de transición permitidos
    event_error_rate:  ["rate<0.01"],
    event_latency_ms:  ["p(95)<500", "p(99)<1000"],
  },

  scenarios: {
    // Escenario 1: Carga sostenida (2 min a 10 VUs)
    sustained_load: {
      executor:  "constant-vus",
      vus:       10,
      duration:  "2m",
      startTime: "0s",
      tags:      { scenario: "sustained" },
    },

    // Escenario 2: Rampa progresiva — detecta degradación
    ramp_up: {
      executor:  "ramping-vus",
      startVUs:  0,
      stages: [
        { duration: "30s", target: 10  },
        { duration: "1m",  target: 30  },
        { duration: "30s", target: 60  },
        { duration: "30s", target: 0   },
      ],
      startTime: "2m30s",
      tags:      { scenario: "ramp" },
    },

    // Escenario 3: Punto de quiebre — sube hasta fallar
    breakpoint: {
      executor:         "ramping-arrival-rate",
      startRate:        5,
      timeUnit:         "1s",
      preAllocatedVUs:  30,
      maxVUs:           150,
      stages: [
        { duration: "1m", target: 30  },
        { duration: "1m", target: 80  },
        { duration: "1m", target: 150 },
      ],
      startTime: "7m30s",
      tags:      { scenario: "breakpoint" },
    },
  },
};

// ── Secuencia de eventos válida según EventType → ShipmentStatus ─────────────
const VALID_SEQUENCE = [
  "DISPATCHED",           // CREATED        → IN_TRANSIT
  "ARRIVED_AT_HUB",       // IN_TRANSIT     → AT_TRANSIT_POINT
  "DEPARTED_FROM_HUB",    // AT_TRANSIT_POINT → IN_TRANSIT
  "OUT_FOR_DELIVERY",     // IN_TRANSIT     → OUT_FOR_DELIVERY
  "DELIVERED",            // OUT_FOR_DELIVERY → DELIVERED
];

function buildPayload(eventType) {
  return JSON.stringify({
    eventType:  eventType,
    location:   "Bogotá - Hub Principal",
    occurredAt: new Date(Date.now() - 5000).toISOString().slice(0, 19),
  });
}

// ── Función principal ────────────────────────────────────────────────────────
export default function () {
  // Cada VU usa un shipmentId diferente (round-robin por VU ID)

  const shipmentId = SHIPMENTS[__VU % SHIPMENTS.length];

  for (const eventType of VALID_SEQUENCE) {
    const url = `${BASE_URL}/${shipmentId}/events`;
    const res = http.post(url, buildPayload(eventType), { headers: JSON_HEADERS });

    const ok = check(res, {
      [`${eventType}: status 202`]:               (r) => r.status === 202,
      [`${eventType}: X-Correlation-Id presente`]: (r) => r.headers["X-Correlation-Id"] !== undefined,
      [`${eventType}: latencia < 500ms`]:          (r) => r.timings.duration < 500,
    });

    eventLatency.add(res.timings.duration);

    if (res.status === 202) {
      acceptedEvents.add(1);
      errorRate.add(0);
    } else {
      transitionErrors.add(1);
      errorRate.add(1);
      // Si falla, no tiene sentido continuar la secuencia para este shipment
      break;
    }

    // Pausa entre eventos (simula procesamiento asíncrono de RabbitMQ)
    sleep(0.3);
  }

  sleep(1);
}