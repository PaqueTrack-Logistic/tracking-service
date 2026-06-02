import { uuidv4 } from "https://jslib.k6.io/k6-utils/1.4.0/index.js";

// Usa un shipmentId REAL de tu BD para evitar 404s
export const KNOWN_SHIPMENT_ID = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"; // ← reemplaza

export function buildEventPayload(eventType = "PICKUP") {
  return JSON.stringify({
    eventType:  eventType,
    location:   "Bogotá - Centro",
    occurredAt: new Date(Date.now() - 60000).toISOString().slice(0, 19), // hace 1 min
  });
}

export const JSON_HEADERS = {
  "Content-Type": "application/json",
  "Accept":       "application/json",
};