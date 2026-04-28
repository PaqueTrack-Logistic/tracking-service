package com.github.camiloperez77.trackingservice.domain.service;


import com.github.camiloperez77.trackingservice.domain.exception.ShipmentNotFoundException;
import com.github.camiloperez77.trackingservice.domain.model.EventType;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
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
    public TrackingEvent registerEvent(UUID shipmentId, EventType eventType, String location, LocalDateTime occurredAt) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found with id: " + shipmentId));

        log.info("Registering event - shipmentId: {}, eventType: {}, location: {}", shipmentId, eventType, location);
        
        ShipmentStatus previousStatus = shipment.getStatus();
        ShipmentStatus newStatus = eventType.getTargetStatus(); // ← clave

        shipment.updateStatus(newStatus);

        log.info("Event registered successfully - shipmentId: {}, eventType: {}, status change: {} -> {}", 
                shipmentId, eventType, previousStatus, newStatus);

        TrackingEvent event = TrackingEvent.builder()
                .id(UUID.randomUUID())
                .shipmentId(shipmentId)
                .eventType(eventType)
                .statusBefore(previousStatus)
                .statusAfter(newStatus)
                .location(location)
                .occurredAt(occurredAt)
                .build();
        eventRepository.save(event);
        shipmentRepository.save(shipment);

        // Publicar evento de notificación (se mantiene como estaba, usando strings)
        TrackingEventNotification notification = new TrackingEventNotification(
                shipmentId,
                shipment.getTrackingId(),
                eventType.name(),   // convertimos a String
                previousStatus.name(),
                newStatus.name(),
                occurredAt
        );
        eventPublisher.publishTrackingEventRecorded(notification);

        return event;
    }

    @Override
    public Page<TrackingEvent> getHistory(UUID shipmentId, Pageable pageable) {
    return eventRepository.findByShipmentIdOrderByOccurredAtAsc(shipmentId, pageable);
    }

    @Override
    public Shipment getCurrentStatus(UUID shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found with id: " + shipmentId));
    }
// Método auxiliar para mantener compatibilidad con el listener y llamadas desde strings
    public TrackingEvent registerEvent(UUID shipmentId, String eventTypeStr, String location, LocalDateTime occurredAt) {
        EventType eventType;
        try {
            eventType = EventType.valueOf(eventTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid event type: " + eventTypeStr);
        }
        return registerEvent(shipmentId, eventType, location, occurredAt);
    }
}
