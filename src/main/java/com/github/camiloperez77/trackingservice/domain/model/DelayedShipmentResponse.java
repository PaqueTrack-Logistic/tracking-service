package com.github.camiloperez77.trackingservice.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record DelayedShipmentResponse(
    UUID shipmentId,
    String trackingId,
    String status,
    LocalDateTime lastEventTime,
    long hoursInCurrentStatus
) {}
