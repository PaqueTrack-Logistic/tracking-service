package com.github.camiloperez77.trackingservice.domain.ports.out;

import com.github.camiloperez77.trackingservice.domain.model.TrackingEvent;
import java.util.List;
import java.util.UUID;

public interface TrackingEventRepositoryPort {
    TrackingEvent save(TrackingEvent event);
    List<TrackingEvent> findByShipmentIdOrderByOccurredAtAsc(UUID shipmentId);
}