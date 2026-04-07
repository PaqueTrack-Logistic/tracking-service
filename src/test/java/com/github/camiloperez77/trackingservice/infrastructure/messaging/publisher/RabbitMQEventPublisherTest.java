package com.github.camiloperez77.trackingservice.infrastructure.messaging.publisher;

import com.github.camiloperez77.trackingservice.domain.model.TrackingEventNotification;
import com.github.camiloperez77.trackingservice.infrastructure.messaging.config.RabbitMQConfig;
import com.github.camiloperez77.trackingservice.infrastructure.messaging.dto.TrackingEventRecordedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitMQEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitMQEventPublisher publisher;

    @Test
    @DisplayName("publishTrackingEventRecorded sends to correct exchange and routing key")
    void publishTrackingEventRecorded_shouldSendToCorrectExchangeAndRoutingKey() {
        UUID shipmentId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.now();
        TrackingEventNotification notification = new TrackingEventNotification(
                shipmentId, "TRK-001", "DISPATCHED", "CREATED", "IN_TRANSIT", occurredAt
        );

        publisher.publishTrackingEventRecorded(notification);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.LOGISTICS_EXCHANGE),
                eq("tracking.event.recorded"),
                org.mockito.ArgumentMatchers.any(TrackingEventRecordedEvent.class)
        );
    }

    @Test
    @DisplayName("publishTrackingEventRecorded message contains correct fields")
    void publishTrackingEventRecorded_shouldContainCorrectFields() {
        UUID shipmentId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.now();
        TrackingEventNotification notification = new TrackingEventNotification(
                shipmentId, "TRK-002", "DELIVERED", "OUT_FOR_DELIVERY", "DELIVERED", occurredAt
        );

        publisher.publishTrackingEventRecorded(notification);

        ArgumentCaptor<TrackingEventRecordedEvent> captor = ArgumentCaptor.forClass(TrackingEventRecordedEvent.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.LOGISTICS_EXCHANGE),
                eq("tracking.event.recorded"),
                captor.capture()
        );

        TrackingEventRecordedEvent event = captor.getValue();
        assertThat(event.getShipmentId()).isEqualTo(shipmentId);
        assertThat(event.getTrackingId()).isEqualTo("TRK-002");
        assertThat(event.getEventType()).isEqualTo("DELIVERED");
        assertThat(event.getPreviousStatus()).isEqualTo("OUT_FOR_DELIVERY");
        assertThat(event.getNewStatus()).isEqualTo("DELIVERED");
        assertThat(event.getOccurredAt()).isEqualTo(occurredAt);
    }
}
