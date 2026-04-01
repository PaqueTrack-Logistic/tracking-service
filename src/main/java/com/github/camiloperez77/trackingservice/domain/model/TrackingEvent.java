package com.github.camiloperez77.trackingservice.domain.model;

import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class TrackingEvent {
    private UUID id;
    private UUID shipmentId;
    private String eventType;
    private ShipmentStatus statusBefore;
    private ShipmentStatus statusAfter;
    private String location;
    private LocalDateTime occurredAt;
    private LocalDateTime createdAt;

    public TrackingEvent(UUID id, UUID shipmentId, String eventType,
                         ShipmentStatus statusBefore, ShipmentStatus statusAfter,
                         String location, LocalDateTime occurredAt) {
        this(id, shipmentId, eventType, statusBefore, statusAfter, location, occurredAt, LocalDateTime.now());
    }

    public TrackingEvent(UUID id, UUID shipmentId, String eventType,
                         ShipmentStatus statusBefore, ShipmentStatus statusAfter,
                         String location, LocalDateTime occurredAt, LocalDateTime createdAt) {
        this.id = id;
        this.shipmentId = shipmentId;
        this.eventType = eventType;
        this.statusBefore = statusBefore;
        this.statusAfter = statusAfter;
        this.location = location;
        this.occurredAt = occurredAt;
        this.createdAt = createdAt;
    }
}
