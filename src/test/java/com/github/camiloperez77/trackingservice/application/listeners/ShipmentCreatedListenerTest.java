package com.github.camiloperez77.trackingservice.application.listeners;

import com.github.camiloperez77.trackingservice.domain.ports.in.TrackingUseCase;
import com.github.camiloperez77.trackingservice.infrastructure.messaging.dto.ShipmentCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ShipmentCreatedListenerTest {

    @Mock
    private TrackingUseCase trackingUseCase;

    @InjectMocks
    private ShipmentCreatedListener listener;

    @Test
    @DisplayName("Receives ShipmentCreatedEvent and calls initializeShipment with correct args")
    void handleShipmentCreated_shouldCallInitializeShipment() {
        UUID shipmentId = UUID.randomUUID();
        String trackingId = "TRK-001";

        ShipmentCreatedEvent event = new ShipmentCreatedEvent(shipmentId, trackingId, "Sender", "Recipient");

        listener.handleShipmentCreated(event);

        verify(trackingUseCase).initializeShipment(shipmentId, trackingId);
    }

    @Test
    @DisplayName("Handles UUID parsing correctly with different UUID formats")
    void handleShipmentCreated_shouldHandleUUIDCorrectly() {
        UUID shipmentId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        String trackingId = "TRK-UUID-TEST";

        ShipmentCreatedEvent event = new ShipmentCreatedEvent(shipmentId, trackingId, "Sender", "Recipient");

        listener.handleShipmentCreated(event);

        verify(trackingUseCase).initializeShipment(shipmentId, trackingId);
    }
}
