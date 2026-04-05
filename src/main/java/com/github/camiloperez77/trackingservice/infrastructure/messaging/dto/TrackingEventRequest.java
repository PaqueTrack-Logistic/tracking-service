package com.github.camiloperez77.trackingservice.infrastructure.messaging.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class TrackingEventRequest implements Serializable {
    private final UUID shipmentId;
    private final String eventType;
    private final String location;
    private final LocalDateTime occurredAt;

    public TrackingEventRequest(UUID shipmentId, String eventType, String location, LocalDateTime occurredAt) {
        this.shipmentId = shipmentId;
        this.eventType = eventType;
        this.location = location;
        this.occurredAt = occurredAt;
    }

    public UUID getShipmentId() {
        return shipmentId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getLocation() {
        return location;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
