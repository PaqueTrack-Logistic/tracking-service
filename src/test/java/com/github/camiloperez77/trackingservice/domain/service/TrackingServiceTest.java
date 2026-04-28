package com.github.camiloperez77.trackingservice.domain.service;

import com.github.camiloperez77.trackingservice.domain.exception.ShipmentNotFoundException;
import com.github.camiloperez77.trackingservice.domain.model.*;
import com.github.camiloperez77.trackingservice.domain.ports.out.EventPublisherPort;
import com.github.camiloperez77.trackingservice.domain.ports.out.ShipmentRepositoryPort;
import com.github.camiloperez77.trackingservice.domain.ports.out.TrackingEventRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackingServiceTest {

    @Mock
    private ShipmentRepositoryPort shipmentRepository;

    @Mock
    private TrackingEventRepositoryPort eventRepository;

    @Mock
    private EventPublisherPort eventPublisher;

    @InjectMocks
    private TrackingService trackingService;

    private UUID shipmentId;
    private Shipment existingShipment;

    @BeforeEach
    void setUp() {
        shipmentId = UUID.randomUUID();
        existingShipment = new Shipment(shipmentId, "TRK-001", ShipmentStatus.CREATED);
    }

    @Test
    void registerEvent_ShouldChangeStatusAndSaveEvent_WhenValid() {
        // given
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(existingShipment));
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(existingShipment);
        when(eventRepository.save(any(TrackingEvent.class))).thenAnswer(i -> i.getArgument(0));

        LocalDateTime now = LocalDateTime.now();
        EventType eventType = EventType.DISPATCHED;

        // when
        TrackingEvent event = trackingService.registerEvent(shipmentId, eventType, "Warehouse A", now);

        // then
        assertEquals(eventType, event.getEventType());
        assertEquals(ShipmentStatus.CREATED, event.getStatusBefore());
        assertEquals(ShipmentStatus.IN_TRANSIT, event.getStatusAfter());
        assertEquals(ShipmentStatus.IN_TRANSIT, existingShipment.getStatus());

        verify(shipmentRepository).save(existingShipment);
        verify(eventRepository).save(any(TrackingEvent.class));
        verify(eventPublisher).publishTrackingEventRecorded(any(TrackingEventNotification.class));
    }

    @Test
    void registerEvent_ShouldThrowException_WhenShipmentNotFound() {
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.empty());

        assertThrows(ShipmentNotFoundException.class,
                () -> trackingService.registerEvent(shipmentId, EventType.DISPATCHED, "Location", LocalDateTime.now()));
    }

    @Test
    void registerEvent_ShouldThrowException_WhenInvalidTransition() {
        // Dado un envío en estado CREATED (sin modificar)
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(existingShipment));
        // Intentar un evento que lleva a DELIVERED directamente
        assertThrows(IllegalStateException.class,
            () -> trackingService.registerEvent(shipmentId, EventType.DELIVERED, "Location", LocalDateTime.now()));
    }

    @Test
    void getCurrentStatus_ShouldReturnShipment_WhenExists() {
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(existingShipment));
        Shipment found = trackingService.getCurrentStatus(shipmentId);
        assertEquals(existingShipment, found);
    }

    @Test
    void getCurrentStatus_ShouldThrowException_WhenNotFound() {
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.empty());
        assertThrows(ShipmentNotFoundException.class,
                () -> trackingService.getCurrentStatus(shipmentId));
    }

    @Test
    void initializeShipment_ShouldCreateNewShipment() {
        UUID newId = UUID.randomUUID();
        when(shipmentRepository.findById(newId)).thenReturn(Optional.empty());
        when(shipmentRepository.findByTrackingId("TRK-NEW")).thenReturn(Optional.empty());
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(i -> i.getArgument(0));

        trackingService.initializeShipment(newId, "TRK-NEW");

        verify(shipmentRepository).save(argThat(shipment ->
                shipment.getId().equals(newId) &&
                shipment.getTrackingId().equals("TRK-NEW") &&
                shipment.getStatus() == ShipmentStatus.CREATED
        ));
    }
}