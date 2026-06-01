package com.github.camiloperez77.trackingservice.application.rest;

import java.time.LocalDateTime;
import java.util.UUID;

public record TransitTimeResponse(
    UUID shipmentId,
    String trackingId,
    String status,
    LocalDateTime createdAt,
    LocalDateTime deliveredAt,
    Double transitTimeHours
) {}