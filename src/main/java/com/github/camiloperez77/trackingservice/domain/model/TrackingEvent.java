package com.github.camiloperez77.trackingservice.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class TrackingEvent {
    private UUID id;
    private UUID shipmentId;
    private EventType eventType;
    private ShipmentStatus statusBefore;
    private ShipmentStatus statusAfter;
    private String location;
    private LocalDateTime occurredAt;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}