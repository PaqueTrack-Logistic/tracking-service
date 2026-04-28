package com.github.camiloperez77.trackingservice.domain.ports.in;

import com.github.camiloperez77.trackingservice.domain.model.EventType;
import com.github.camiloperez77.trackingservice.domain.model.Shipment;
import com.github.camiloperez77.trackingservice.domain.model.TrackingEvent;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TrackingUseCase {
    void initializeShipment(UUID shipmentId, String trackingId);
    TrackingEvent registerEvent(UUID shipmentId, EventType eventType, String location, LocalDateTime occurredAt);
    TrackingEvent registerEvent(UUID shipmentId, String eventType, String location, LocalDateTime occurredAt);
    Page<TrackingEvent> getHistory(UUID shipmentId, Pageable pageable);
    Shipment getCurrentStatus(UUID shipmentId);
}
