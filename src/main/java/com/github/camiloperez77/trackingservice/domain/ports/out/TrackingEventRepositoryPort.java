package com.github.camiloperez77.trackingservice.domain.ports.out;

import com.github.camiloperez77.trackingservice.domain.model.TrackingEvent;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TrackingEventRepositoryPort {
    TrackingEvent save(TrackingEvent event);
    Page<TrackingEvent> findByShipmentIdOrderByOccurredAtAsc(UUID shipmentId, Pageable pageable);
}