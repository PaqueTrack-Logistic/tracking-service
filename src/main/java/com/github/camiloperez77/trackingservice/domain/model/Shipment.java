package com.github.camiloperez77.trackingservice.domain.model;

import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Shipment {
    private UUID id;
    private String trackingId;
    private ShipmentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Shipment(UUID id, String trackingId, ShipmentStatus status) {
        this(id, trackingId, status, LocalDateTime.now(), LocalDateTime.now());
    }

    public Shipment(UUID id, String trackingId, ShipmentStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.trackingId = trackingId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void updateStatus(ShipmentStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                String.format("Invalid status transition from %s to %s", this.status, newStatus)
            );
        }
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }
}
