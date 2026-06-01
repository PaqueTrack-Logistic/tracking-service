package com.github.camiloperez77.trackingservice.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record DelayedShipment(
    UUID shipmentId,
    String trackingId,
    ShipmentStatus status,
    LocalDateTime lastEventTime,
    long hoursInCurrentStatus
) {}