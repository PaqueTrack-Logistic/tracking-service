package com.github.camiloperez77.trackingservice.domain.service;


import com.github.camiloperez77.trackingservice.domain.model.Shipment;
import com.github.camiloperez77.trackingservice.domain.model.ShipmentStatus;
import com.github.camiloperez77.trackingservice.domain.model.TrackingEvent;
import com.github.camiloperez77.trackingservice.domain.model.TrackingEventNotification;
import com.github.camiloperez77.trackingservice.domain.ports.in.TrackingUseCase;
import com.github.camiloperez77.trackingservice.domain.ports.out.ShipmentRepositoryPort;
import com.github.camiloperez77.trackingservice.domain.ports.out.TrackingEventRepositoryPort;
import com.github.camiloperez77.trackingservice.domain.ports.out.EventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingService implements TrackingUseCase {

    private final ShipmentRepositoryPort shipmentRepository;
    private final TrackingEventRepositoryPort eventRepository;
    private final EventPublisherPort eventPublisher;

    @Override
    @Transactional
    public void initializeShipment(UUID shipmentId, String trackingId) {
        Shipment existingById = shipmentRepository.findById(shipmentId).orElse(null);
        if (existingById != null) {
            if (!existingById.getTrackingId().equals(trackingId)) {
                throw new IllegalStateException(
                        String.format(
                                "Shipment id %s already exists with trackingId %s and cannot be reinitialized with %s",
                                shipmentId,
                                existingById.getTrackingId(),
                                trackingId
                        )
                );
            }

            log.info("Ignoring duplicate shipment.created for shipmentId: {}, trackingId: {}", shipmentId, trackingId);
            return;
        }

        shipmentRepository.findByTrackingId(trackingId).ifPresent(existingShipment -> {
            throw new IllegalStateException(
                    String.format(
                            "Tracking id %s is already assigned to shipment %s",
                            trackingId,
                            existingShipment.getId()
                    )
            );
        });

        Shipment shipment = new Shipment(shipmentId, trackingId, ShipmentStatus.CREATED);
        try {
            shipmentRepository.save(shipment);
            log.info("Initialized shipment with id: {}, trackingId: {}", shipmentId, trackingId);
        } catch (DataIntegrityViolationException ex) {
            Shipment concurrentById = shipmentRepository.findById(shipmentId).orElse(null);
            if (concurrentById != null && concurrentById.getTrackingId().equals(trackingId)) {
                log.info("Ignoring duplicate shipment.created detected by unique constraint for shipmentId: {}, trackingId: {}", shipmentId, trackingId);
                return;
            }

            Shipment concurrentByTracking = shipmentRepository.findByTrackingId(trackingId).orElse(null);
            if (concurrentByTracking != null && concurrentByTracking.getId().equals(shipmentId)) {
                log.info("Ignoring duplicate shipment.created detected by trackingId unique constraint for shipmentId: {}, trackingId: {}", shipmentId, trackingId);
                return;
            }

            throw new IllegalStateException("Conflicting duplicate shipment.created detected", ex);
        }
    }

    @Override
    @Transactional
    public TrackingEvent registerEvent(UUID shipmentId, String eventType, String location, LocalDateTime occurredAt) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found with id: " + shipmentId));

        ShipmentStatus previousStatus = shipment.getStatus();
        ShipmentStatus newStatus = mapEventTypeToStatus(eventType);

        // Lógica de transición (valida)
        shipment.updateStatus(newStatus);

        TrackingEvent event = new TrackingEvent(
                UUID.randomUUID(),
                shipmentId,
                eventType,
                previousStatus,
                newStatus,
                location,
                occurredAt
        );
        eventRepository.save(event);
        shipmentRepository.save(shipment);

        // Publicar evento
        TrackingEventNotification notification = new TrackingEventNotification(
                shipmentId,
                shipment.getTrackingId(),
                eventType,
                previousStatus.name(),
                newStatus.name(),
                occurredAt
        );
        eventPublisher.publishTrackingEventRecorded(notification);

        return event;
    }

    @Override
    public List<TrackingEvent> getHistory(UUID shipmentId) {
        return eventRepository.findByShipmentIdOrderByOccurredAtAsc(shipmentId);
    }

    @Override
    public Shipment getCurrentStatus(UUID shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found with id: " + shipmentId));
    }

    private ShipmentStatus mapEventTypeToStatus(String eventType) {
        return switch (eventType.toUpperCase()) {
            case "DISPATCHED" -> ShipmentStatus.IN_TRANSIT;
            case "OUT_FOR_DELIVERY" -> ShipmentStatus.OUT_FOR_DELIVERY;
            case "DELIVERED" -> ShipmentStatus.DELIVERED;
            default -> ShipmentStatus.EXCEPTION;
        };
    }
}
