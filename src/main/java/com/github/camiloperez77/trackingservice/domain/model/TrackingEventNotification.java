package com.github.camiloperez77.trackingservice.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record TrackingEventNotification(
        UUID shipmentId,
        String trackingId,
        String eventType,
        String previousStatus,
        String newStatus,
        LocalDateTime occurredAt
) {
}
