package com.github.camiloperez77.trackingservice.infrastructure.messaging.publisher;

import com.github.camiloperez77.trackingservice.domain.model.TrackingEventNotification;
import com.github.camiloperez77.trackingservice.domain.ports.out.EventPublisherPort;
import com.github.camiloperez77.trackingservice.infrastructure.messaging.dto.TrackingEventRecordedEvent;
import com.github.camiloperez77.trackingservice.infrastructure.messaging.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQEventPublisher implements EventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishTrackingEventRecorded(TrackingEventNotification notification) {
        TrackingEventRecordedEvent event = new TrackingEventRecordedEvent(
                notification.shipmentId(),
                notification.trackingId(),
                notification.eventType(),
                notification.previousStatus(),
                notification.newStatus(),
                notification.occurredAt()
        );

        log.info("Publishing tracking.event.recorded: {}", event);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.LOGISTICS_EXCHANGE,
                "tracking.event.recorded",
                event
        );
    }
}