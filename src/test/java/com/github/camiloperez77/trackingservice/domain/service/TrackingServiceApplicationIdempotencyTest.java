package com.github.camiloperez77.trackingservice.domain.service;

import com.github.camiloperez77.trackingservice.domain.model.Shipment;
import com.github.camiloperez77.trackingservice.domain.model.TrackingEvent;
import com.github.camiloperez77.trackingservice.domain.model.TrackingEventNotification;
import com.github.camiloperez77.trackingservice.domain.ports.out.EventPublisherPort;
import com.github.camiloperez77.trackingservice.domain.ports.out.ShipmentRepositoryPort;
import com.github.camiloperez77.trackingservice.domain.ports.out.TrackingEventRepositoryPort;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

class TrackingServiceIdempotencyTest {

    @Test
    void initializeShipment_ShouldCreateShipment_WhenFirstEventArrives() {
        InMemoryShipmentRepository shipmentRepository = new InMemoryShipmentRepository();
        TrackingService service = new TrackingService(shipmentRepository, new NoOpTrackingEventRepository(), new NoOpEventPublisher());

        UUID shipmentId = UUID.randomUUID();
        service.initializeShipment(shipmentId, "TRK-001");

        assertEquals(1, shipmentRepository.storageById.size());
        assertEquals("TRK-001", shipmentRepository.storageById.get(shipmentId).getTrackingId());
    }

    @Test
    void initializeShipment_ShouldIgnoreDuplicate_WhenShipmentAndTrackingMatch() {
        InMemoryShipmentRepository shipmentRepository = new InMemoryShipmentRepository();
        TrackingService service = new TrackingService(shipmentRepository, new NoOpTrackingEventRepository(), new NoOpEventPublisher());

        UUID shipmentId = UUID.randomUUID();
        service.initializeShipment(shipmentId, "TRK-001");

        assertDoesNotThrow(() -> service.initializeShipment(shipmentId, "TRK-001"));
        assertEquals(1, shipmentRepository.storageById.size());
    }

    @Test
    void initializeShipment_ShouldFail_WhenSameShipmentIdUsesDifferentTrackingId() {
        InMemoryShipmentRepository shipmentRepository = new InMemoryShipmentRepository();
        TrackingService service = new TrackingService(shipmentRepository, new NoOpTrackingEventRepository(), new NoOpEventPublisher());

        UUID shipmentId = UUID.randomUUID();
        service.initializeShipment(shipmentId, "TRK-001");

        assertThrows(IllegalStateException.class, () -> service.initializeShipment(shipmentId, "TRK-002"));
    }

    @Test
    void initializeShipment_ShouldFail_WhenTrackingIdAlreadyBelongsToAnotherShipment() {
        InMemoryShipmentRepository shipmentRepository = new InMemoryShipmentRepository();
        TrackingService service = new TrackingService(shipmentRepository, new NoOpTrackingEventRepository(), new NoOpEventPublisher());

        service.initializeShipment(UUID.randomUUID(), "TRK-001");

        assertThrows(IllegalStateException.class, () -> service.initializeShipment(UUID.randomUUID(), "TRK-001"));
    }

    private static class InMemoryShipmentRepository implements ShipmentRepositoryPort {
        private final Map<UUID, Shipment> storageById = new HashMap<>();

        @Override
        public Shipment save(Shipment shipment) {
            storageById.put(shipment.getId(), shipment);
            return shipment;
        }

        @Override
        public Optional<Shipment> findById(UUID id) {
            return Optional.ofNullable(storageById.get(id));
        }

        @Override
        public Optional<Shipment> findByTrackingId(String trackingId) {
            return storageById.values().stream()
                    .filter(shipment -> Objects.equals(shipment.getTrackingId(), trackingId))
                    .findFirst();
        }
    }

    private static class NoOpTrackingEventRepository implements TrackingEventRepositoryPort { @Override
        public TrackingEvent save(TrackingEvent event) {
            return event;
        }

        @Override
        public Page<TrackingEvent> findByShipmentIdOrderByOccurredAtAsc(UUID shipmentId, Pageable pageable) {
            // Retorna una página vacía (sin datos, sin contenido)
            return Page.empty(pageable);
        }
    }

    private static class NoOpEventPublisher implements EventPublisherPort {
        @Override
        public void publishTrackingEventRecorded(TrackingEventNotification notification) {
            // no-op
        }
    }
}
