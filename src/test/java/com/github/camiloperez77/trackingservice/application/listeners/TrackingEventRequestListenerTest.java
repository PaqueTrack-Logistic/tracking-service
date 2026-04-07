package com.github.camiloperez77.trackingservice.application.listeners;

import com.github.camiloperez77.trackingservice.domain.ports.in.TrackingUseCase;
import com.github.camiloperez77.trackingservice.infrastructure.messaging.dto.TrackingEventRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackingEventRequestListenerTest {

    @Mock
    private TrackingUseCase trackingUseCase;

    @InjectMocks
    private TrackingEventRequestListener listener;

    @Test
    @DisplayName("Receives TrackingEventRequest and calls registerEvent with correct args")
    void handleTrackingEventRequest_shouldCallRegisterEvent() {
        UUID shipmentId = UUID.randomUUID();
        String eventType = "DISPATCHED";
        String location = "Hub Central";
        LocalDateTime occurredAt = LocalDateTime.now();

        TrackingEventRequest request = new TrackingEventRequest(shipmentId, eventType, location, occurredAt);

        listener.handleTrackingEventRequest(request);

        verify(trackingUseCase).registerEvent(shipmentId, eventType, location, occurredAt);
    }

    @Test
    @DisplayName("Propagates exceptions wrapped in IllegalStateException for dead-letter queue handling")
    void handleTrackingEventRequest_shouldPropagateExceptions() {
        UUID shipmentId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.now();

        TrackingEventRequest request = new TrackingEventRequest(shipmentId, "DISPATCHED", "Hub", occurredAt);

        when(trackingUseCase.registerEvent(shipmentId, "DISPATCHED", "Hub", occurredAt))
                .thenThrow(new IllegalArgumentException("Shipment not found"));

        assertThatThrownBy(() -> listener.handleTrackingEventRequest(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to process tracking event request")
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }
}
