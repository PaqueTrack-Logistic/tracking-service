package com.github.camiloperez77.trackingservice.domain.model;

public enum ShipmentStatus {
    CREATED,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    EXCEPTION;

    public boolean canTransitionTo(ShipmentStatus nextStatus) {
        switch (this) {
            case CREATED:
                return nextStatus == IN_TRANSIT;
            case IN_TRANSIT:
                return nextStatus == OUT_FOR_DELIVERY || nextStatus == EXCEPTION;
            case OUT_FOR_DELIVERY:
                return nextStatus == DELIVERED || nextStatus == EXCEPTION;
            case DELIVERED:
                return false; // terminal
            case EXCEPTION:
                return nextStatus == CREATED || nextStatus == IN_TRANSIT;
            default:
                return false;
        }
    }
}