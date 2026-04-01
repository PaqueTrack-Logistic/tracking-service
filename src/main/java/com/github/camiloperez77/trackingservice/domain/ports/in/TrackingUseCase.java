package com.github.camiloperez77.trackingservice.domain.ports.in;

import com.github.camiloperez77.trackingservice.domain.model.Shipment;
import com.github.camiloperez77.trackingservice.domain.model.TrackingEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TrackingUseCase {
    void initializeShipment(UUID shipmentId, String trackingId);
    TrackingEvent registerEvent(UUID shipmentId, String eventType, String location, LocalDateTime occurredAt);
    List<TrackingEvent> getHistory(UUID shipmentId);
    Shipment getCurrentStatus(UUID shipmentId);
}
