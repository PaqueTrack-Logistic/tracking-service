package com.github.camiloperez77.trackingservice.application.listeners;

import com.github.camiloperez77.trackingservice.domain.ports.in.TrackingUseCase;
import com.github.camiloperez77.trackingservice.infrastructure.messaging.dto.ShipmentCreatedEvent;
import com.github.camiloperez77.trackingservice.infrastructure.messaging.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentCreatedListener {

    private final TrackingUseCase trackingUseCase;

    @RabbitListener(queues = RabbitMQConfig.SHIPMENT_CREATED_QUEUE)
    public void handleShipmentCreated(ShipmentCreatedEvent event) {
        log.info("Received shipment.created event: {}", event);
        trackingUseCase.initializeShipment(event.getShipmentId(), event.getTrackingId());
    }
}