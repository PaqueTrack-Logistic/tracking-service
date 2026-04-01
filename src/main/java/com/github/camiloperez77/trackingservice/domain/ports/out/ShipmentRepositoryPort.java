package com.github.camiloperez77.trackingservice.domain.ports.out;

import com.github.camiloperez77.trackingservice.domain.model.Shipment;
import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepositoryPort {
    Shipment save(Shipment shipment);
    Optional<Shipment> findById(UUID id);
    Optional<Shipment> findByTrackingId(String trackingId);
}