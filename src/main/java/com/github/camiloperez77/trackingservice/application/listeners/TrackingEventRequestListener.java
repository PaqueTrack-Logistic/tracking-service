package com.github.camiloperez77.trackingservice.application.listeners;

import com.github.camiloperez77.trackingservice.domain.ports.in.TrackingUseCase;
import com.github.camiloperez77.trackingservice.infrastructure.messaging.dto.TrackingEventRequest;
import com.github.camiloperez77.trackingservice.infrastructure.messaging.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrackingEventRequestListener {

    private final TrackingUseCase trackingUseCase;

    @RabbitListener(queues = RabbitMQConfig.TRACKING_EVENT_REQUEST_QUEUE)
    public void handleTrackingEventRequest(TrackingEventRequest request) {
        log.info("Processing async tracking event request: {}", request);
        try {
            trackingUseCase.registerEvent(
                    request.getShipmentId(),
                    request.getEventType(),
                    request.getLocation(),
                    request.getOccurredAt()
            );
        } catch (Exception e) {
            log.error("Error processing tracking event request: {}", request, e);
            throw new IllegalStateException("Failed to process tracking event request", e);
        }
    }
}