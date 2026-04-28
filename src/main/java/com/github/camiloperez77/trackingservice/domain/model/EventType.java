package com.github.camiloperez77.trackingservice.domain.model;

import lombok.Getter;

@Getter
public enum EventType {
    DISPATCHED(ShipmentStatus.IN_TRANSIT),
    ARRIVED_AT_HUB(ShipmentStatus.AT_TRANSIT_POINT),
    DEPARTED_FROM_HUB(ShipmentStatus.IN_TRANSIT),
    ARRIVED_AT_TERMINAL(ShipmentStatus.AT_TRANSIT_POINT),
    DEPARTED_FROM_TERMINAL(ShipmentStatus.IN_TRANSIT),
    OUT_FOR_DELIVERY(ShipmentStatus.OUT_FOR_DELIVERY),
    DELIVERED(ShipmentStatus.DELIVERED),
    DAMAGED(ShipmentStatus.EXCEPTION);

    private final ShipmentStatus targetStatus;

    EventType(ShipmentStatus targetStatus) {
        this.targetStatus = targetStatus;
    }
}