package com.github.camiloperez77.trackingservice.domain.ports.out;

import com.github.camiloperez77.trackingservice.domain.model.Shipment;
import com.github.camiloperez77.trackingservice.domain.model.ShipmentStatus;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface ShipmentRepositoryPort {
    Shipment save(Shipment shipment);
    Optional<Shipment> findById(UUID id);
    Optional<Shipment> findByTrackingId(String trackingId);
    List<Shipment> findByStatusIn(List<ShipmentStatus> statuses);
}