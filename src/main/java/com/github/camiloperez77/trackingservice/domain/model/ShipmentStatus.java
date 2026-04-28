package com.github.camiloperez77.trackingservice.domain.model;

@SuppressWarnings("fallthrough")
public enum ShipmentStatus {
    CREATED,
    IN_TRANSIT,           // en movimiento entre centros
    AT_TRANSIT_POINT,     // detenido en un centro logístico (hub, terminal, puerto)
    OUT_FOR_DELIVERY,
    DELIVERED,
    EXCEPTION;

    public boolean canTransitionTo(ShipmentStatus nextStatus) {
        // No permitir auto-transición (mismo estado)
        if (this == nextStatus) {
            return false;
        }
        switch (this) {
            case CREATED:
                return nextStatus == IN_TRANSIT || nextStatus == AT_TRANSIT_POINT;
            case IN_TRANSIT:
                return nextStatus == AT_TRANSIT_POINT || nextStatus == OUT_FOR_DELIVERY || nextStatus == EXCEPTION;
            case AT_TRANSIT_POINT:
                return nextStatus == IN_TRANSIT || nextStatus == OUT_FOR_DELIVERY || nextStatus == EXCEPTION;
            case OUT_FOR_DELIVERY:
                return nextStatus == DELIVERED || nextStatus == EXCEPTION;
            case DELIVERED:
                return false;
            case EXCEPTION:
                return nextStatus == CREATED || nextStatus == IN_TRANSIT || nextStatus == AT_TRANSIT_POINT;
            default:
                return false;
        }
    }
}