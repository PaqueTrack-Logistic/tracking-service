package com.github.camiloperez77.trackingservice.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record TransitTime(
    UUID shipmentId,
    String trackingId,
    ShipmentStatus status,
    LocalDateTime createdAt,
    LocalDateTime deliveredAt,
    Double transitTimeHours
) {}