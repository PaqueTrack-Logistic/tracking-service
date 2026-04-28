package com.github.camiloperez77.trackingservice.domain.service;

import com.github.camiloperez77.trackingservice.domain.model.EventType;
import com.github.camiloperez77.trackingservice.domain.model.Shipment;
import com.github.camiloperez77.trackingservice.domain.model.ShipmentStatus;
import com.github.camiloperez77.trackingservice.domain.model.TrackingEvent;
import com.github.camiloperez77.trackingservice.domain.model.TrackingEventNotification;
import com.github.camiloperez77.trackingservice.domain.exception.ShipmentNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.github.camiloperez77.trackingservice.domain.ports.out.EventPublisherPort;
import com.github.camiloperez77.trackingservice.domain.ports.out.ShipmentRepositoryPort;
import com.github.camiloperez77.trackingservice.domain.ports.out.TrackingEventRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackingServiceApplicationTest {

    @Mock
    private ShipmentRepositoryPort shipmentRepository;

    @Mock
    private TrackingEventRepositoryPort eventRepository;

    @Mock
    private EventPublisherPort eventPublisher;

    @InjectMocks
    private TrackingService trackingService;

    @Test
    @DisplayName("registerEvent with valid transition creates event and updates shipment")
    void registerEvent_validTransition_createsEventAndUpdatesShipment() {
        UUID shipmentId = UUID.randomUUID();
        Shipment shipment = new Shipment(shipmentId, "TRK-001", ShipmentStatus.CREATED);
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
        when(eventRepository.save(any(TrackingEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));

        LocalDateTime occurredAt = LocalDateTime.now();
        TrackingEvent result = trackingService.registerEvent(shipmentId, "DISPATCHED", "Hub Central", occurredAt);

        assertThat(result).isNotNull();
        assertThat(result.getShipmentId()).isEqualTo(shipmentId);
        assertThat(result.getEventType()).isEqualTo(EventType.DISPATCHED);
        assertThat(result.getStatusBefore()).isEqualTo(ShipmentStatus.CREATED);
        assertThat(result.getStatusAfter()).isEqualTo(ShipmentStatus.IN_TRANSIT);
        assertThat(result.getLocation()).isEqualTo("Hub Central");

        verify(eventRepository).save(any(TrackingEvent.class));
        verify(shipmentRepository).save(shipment);
    }

    @Test
    @DisplayName("CP-04-02: registerEvent with invalid transition throws IllegalStateException")
    void registerEvent_invalidTransition_throwsIllegalState() {
        UUID shipmentId = UUID.randomUUID();
        Shipment shipment = new Shipment(shipmentId, "TRK-001", ShipmentStatus.CREATED);
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));

        LocalDateTime now = LocalDateTime.now();

        assertThatThrownBy(() ->
                trackingService.registerEvent(shipmentId, "DELIVERED", "Warehouse", now)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    @DisplayName("registerEvent with shipment not found throws ShipmentNotFoundException")
    void registerEvent_shipmentNotFound_throwsShipmentNotFound() {
        UUID shipmentId = UUID.randomUUID();
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.empty());

        LocalDateTime now = LocalDateTime.now();

        assertThatThrownBy(() ->
                trackingService.registerEvent(shipmentId, "DISPATCHED", "Hub", now)
        ).isInstanceOf(ShipmentNotFoundException.class);
    }

    @Test
    @DisplayName("registerEvent publishes notification")
    void registerEvent_publishesNotification() {
        UUID shipmentId = UUID.randomUUID();
        Shipment shipment = new Shipment(shipmentId, "TRK-001", ShipmentStatus.CREATED);
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
        when(eventRepository.save(any(TrackingEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));

        LocalDateTime occurredAt = LocalDateTime.now();
        trackingService.registerEvent(shipmentId, "DISPATCHED", "Hub Central", occurredAt);

        ArgumentCaptor<TrackingEventNotification> captor = ArgumentCaptor.forClass(TrackingEventNotification.class);
        verify(eventPublisher).publishTrackingEventRecorded(captor.capture());

        TrackingEventNotification notification = captor.getValue();
        assertThat(notification.shipmentId()).isEqualTo(shipmentId);
        assertThat(notification.trackingId()).isEqualTo("TRK-001");
        assertThat(notification.eventType()).isEqualTo("DISPATCHED");
        assertThat(notification.previousStatus()).isEqualTo("CREATED");
        assertThat(notification.newStatus()).isEqualTo("IN_TRANSIT");
        assertThat(notification.occurredAt()).isEqualTo(occurredAt);
    }

    @Test
    @DisplayName("CP-04-01: registerEvent DISPATCHED maps to IN_TRANSIT")
    void registerEvent_DISPATCHED_mapsToIN_TRANSIT() {
        UUID shipmentId = UUID.randomUUID();
        Shipment shipment = new Shipment(shipmentId, "TRK-001", ShipmentStatus.CREATED);
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
        when(eventRepository.save(any(TrackingEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));

        TrackingEvent result = trackingService.registerEvent(shipmentId, "DISPATCHED", "Hub", LocalDateTime.now());

        assertThat(result.getStatusAfter()).isEqualTo(ShipmentStatus.IN_TRANSIT);
    }

    @Test
    @DisplayName("registerEvent DELIVERED maps to DELIVERED")
    void registerEvent_DELIVERED_mapsToDELIVERED() {
        UUID shipmentId = UUID.randomUUID();
        Shipment shipment = new Shipment(shipmentId, "TRK-001", ShipmentStatus.OUT_FOR_DELIVERY,
                LocalDateTime.now(), LocalDateTime.now());
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
        when(eventRepository.save(any(TrackingEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));

        TrackingEvent result = trackingService.registerEvent(shipmentId, "DELIVERED", "Destination", LocalDateTime.now());

        assertThat(result.getStatusAfter()).isEqualTo(ShipmentStatus.DELIVERED);
    }

    @Test
    @DisplayName("registerEvent unknown event type throws IllegalArgumentException")
    void registerEvent_unknownEventType_throwsIllegalArgument() {
        UUID shipmentId = UUID.randomUUID();

        assertThatThrownBy(() -> trackingService.registerEvent(shipmentId, "UNKNOWN_TYPE", "Somewhere", LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid event type");
    }

    @Test
    @DisplayName("getHistory returns ordered events")
    void getHistory_returnsOrderedEvents() {
        UUID shipmentId = UUID.randomUUID();
        LocalDateTime time1 = LocalDateTime.now().minusHours(2);
        LocalDateTime time2 = LocalDateTime.now().minusHours(1);
        TrackingEvent event1 = TrackingEvent.builder()
                .id(UUID.randomUUID())
                .shipmentId(shipmentId)
                .eventType(EventType.DISPATCHED)
                .statusBefore(ShipmentStatus.CREATED)
                .statusAfter(ShipmentStatus.IN_TRANSIT)
                .location("Hub A")
                .occurredAt(time1)
                .build();
        TrackingEvent event2 = TrackingEvent.builder()
                .id(UUID.randomUUID())
                .shipmentId(shipmentId)
                .eventType(EventType.OUT_FOR_DELIVERY)
                .statusBefore(ShipmentStatus.IN_TRANSIT)
                .statusAfter(ShipmentStatus.OUT_FOR_DELIVERY)
                .location("Hub B")
                .occurredAt(time2)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        when(eventRepository.findByShipmentIdOrderByOccurredAtAsc(shipmentId, pageable))
                .thenReturn(new PageImpl<>(List.of(event1, event2), pageable, 2));

        Page<TrackingEvent> history = trackingService.getHistory(shipmentId, pageable);

        assertThat(history.getContent()).hasSize(2);
        assertThat(history.getContent().get(0).getOccurredAt()).isBefore(history.getContent().get(1).getOccurredAt());
    }

    @Test
    @DisplayName("getHistory for non-existent shipment returns empty list (delegated to repository)")
    void getHistory_shipmentNotFound_returnsEmptyList() {
        UUID shipmentId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        when(eventRepository.findByShipmentIdOrderByOccurredAtAsc(shipmentId, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<TrackingEvent> history = trackingService.getHistory(shipmentId, pageable);

        assertThat(history.getContent()).isEmpty();
    }

    @Test
    @DisplayName("getCurrentStatus returns shipment")
    void getCurrentStatus_returnsShipment() {
        UUID shipmentId = UUID.randomUUID();
        Shipment shipment = new Shipment(shipmentId, "TRK-001", ShipmentStatus.IN_TRANSIT);
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));

        Shipment result = trackingService.getCurrentStatus(shipmentId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(shipmentId);
        assertThat(result.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
    }

    @Test
    @DisplayName("getCurrentStatus with shipment not found throws ShipmentNotFoundException")
    void getCurrentStatus_shipmentNotFound_throwsShipmentNotFound() {
        UUID shipmentId = UUID.randomUUID();
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trackingService.getCurrentStatus(shipmentId))
                .isInstanceOf(ShipmentNotFoundException.class);
    }

    // --- initializeShipment tests ---

    @Test
    @DisplayName("initializeShipment creates new shipment when none exists")
    void initializeShipment_newShipment_savesSuccessfully() {
        UUID shipmentId = UUID.randomUUID();
        String trackingId = "TRK-NEW";
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.empty());
        when(shipmentRepository.findByTrackingId(trackingId)).thenReturn(Optional.empty());
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));

        trackingService.initializeShipment(shipmentId, trackingId);

        verify(shipmentRepository).save(any(Shipment.class));
    }

    @Test
    @DisplayName("initializeShipment with duplicate shipmentId and same trackingId is idempotent")
    void initializeShipment_duplicateWithSameTrackingId_ignored() {
        UUID shipmentId = UUID.randomUUID();
        String trackingId = "TRK-DUP";
        Shipment existing = new Shipment(shipmentId, trackingId, ShipmentStatus.CREATED);
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(existing));

        trackingService.initializeShipment(shipmentId, trackingId);

        verify(shipmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("initializeShipment with existing shipmentId but different trackingId throws IllegalStateException")
    void initializeShipment_existingIdDifferentTrackingId_throws() {
        UUID shipmentId = UUID.randomUUID();
        Shipment existing = new Shipment(shipmentId, "TRK-ORIGINAL", ShipmentStatus.CREATED);
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> trackingService.initializeShipment(shipmentId, "TRK-DIFFERENT"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already exists with trackingId")
                .hasMessageContaining("TRK-ORIGINAL")
                .hasMessageContaining("TRK-DIFFERENT");
    }

    @Test
    @DisplayName("initializeShipment with trackingId already assigned to another shipment throws IllegalStateException")
    void initializeShipment_trackingIdAlreadyUsed_throws() {
        UUID newShipmentId = UUID.randomUUID();
        UUID existingShipmentId = UUID.randomUUID();
        String trackingId = "TRK-TAKEN";
        Shipment existingByTracking = new Shipment(existingShipmentId, trackingId, ShipmentStatus.CREATED);

        when(shipmentRepository.findById(newShipmentId)).thenReturn(Optional.empty());
        when(shipmentRepository.findByTrackingId(trackingId)).thenReturn(Optional.of(existingByTracking));

        assertThatThrownBy(() -> trackingService.initializeShipment(newShipmentId, trackingId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already assigned to shipment");
    }

    @Test
    @DisplayName("initializeShipment DataIntegrityViolation with concurrent same shipment is idempotent")
    void initializeShipment_dataIntegrityViolation_concurrentSameShipment_ignored() {
        UUID shipmentId = UUID.randomUUID();
        String trackingId = "TRK-CONCURRENT";

        when(shipmentRepository.findById(shipmentId))
                .thenReturn(Optional.empty())  // first call during initial check
                .thenReturn(Optional.of(new Shipment(shipmentId, trackingId, ShipmentStatus.CREATED)));  // second call in catch block
        when(shipmentRepository.findByTrackingId(trackingId)).thenReturn(Optional.empty());
        when(shipmentRepository.save(any(Shipment.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key"));

        trackingService.initializeShipment(shipmentId, trackingId);

        verify(shipmentRepository).save(any(Shipment.class));
    }

    @Test
    @DisplayName("initializeShipment DataIntegrityViolation with concurrent different shipment throws")
    void initializeShipment_dataIntegrityViolation_conflicting_throws() {
        UUID shipmentId = UUID.randomUUID();
        String trackingId = "TRK-CONFLICT";

        when(shipmentRepository.findById(shipmentId))
                .thenReturn(Optional.empty())  // first call
                .thenReturn(Optional.empty());  // second call in catch block - not found by id
        when(shipmentRepository.findByTrackingId(trackingId))
                .thenReturn(Optional.empty())  // first call
                .thenReturn(Optional.empty());  // second call in catch block - not found by tracking either
        when(shipmentRepository.save(any(Shipment.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key"));

        assertThatThrownBy(() -> trackingService.initializeShipment(shipmentId, trackingId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Conflicting duplicate");
    }

    @Test
    @DisplayName("initializeShipment DataIntegrityViolation with concurrent trackingId match is idempotent")
    void initializeShipment_dataIntegrityViolation_concurrentByTrackingId_ignored() {
        UUID shipmentId = UUID.randomUUID();
        String trackingId = "TRK-TRACKING-CONCURRENT";

        when(shipmentRepository.findById(shipmentId))
                .thenReturn(Optional.empty())  // first call
                .thenReturn(Optional.empty());  // second call in catch - not found by id
        when(shipmentRepository.findByTrackingId(trackingId))
                .thenReturn(Optional.empty())  // first call
                .thenReturn(Optional.of(new Shipment(shipmentId, trackingId, ShipmentStatus.CREATED)));  // second call in catch block
        when(shipmentRepository.save(any(Shipment.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key"));

        trackingService.initializeShipment(shipmentId, trackingId);

        verify(shipmentRepository).save(any(Shipment.class));
    }

    @Test
    @DisplayName("registerEvent OUT_FOR_DELIVERY maps to OUT_FOR_DELIVERY status")
    void registerEvent_OUT_FOR_DELIVERY_mapsToOUT_FOR_DELIVERY() {
        UUID shipmentId = UUID.randomUUID();
        // Usar AT_TRANSIT_POINT como estado anterior (válido para transicionar a OUT_FOR_DELIVERY)
        Shipment shipment = new Shipment(shipmentId, "TRK-001", ShipmentStatus.AT_TRANSIT_POINT,
                LocalDateTime.now(), LocalDateTime.now());
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
        when(eventRepository.save(any(TrackingEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));

        TrackingEvent result = trackingService.registerEvent(shipmentId, "OUT_FOR_DELIVERY", "Local Hub", LocalDateTime.now());

        assertThat(result.getStatusAfter()).isEqualTo(ShipmentStatus.OUT_FOR_DELIVERY);
        assertThat(result.getStatusBefore()).isEqualTo(ShipmentStatus.AT_TRANSIT_POINT);
    }
}
