package com.github.camiloperez77.trackingservice.application.rest;

import com.github.camiloperez77.trackingservice.domain.model.Shipment;
import com.github.camiloperez77.trackingservice.domain.model.TrackingEvent;
import com.github.camiloperez77.trackingservice.domain.ports.in.TrackingUseCase;
import com.github.camiloperez77.trackingservice.infrastructure.messaging.config.RabbitMQConfig;
import com.github.camiloperez77.trackingservice.infrastructure.messaging.dto.TrackingEventRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tracking")
@RequiredArgsConstructor
@Tag(name = "Tracking", description = "APIs para gestionar el seguimiento de envíos")
@Slf4j
public class TrackingController {

    private final TrackingUseCase trackingUseCase;
    private final RabbitTemplate rabbitTemplate;

    @PostMapping("/{shipmentId}/events")
    @Operation(summary = "Registrar un evento de tracking", description = "Registra un nuevo evento de seguimiento para un envío")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Evento aceptado para procesamiento asíncrono"),
            @ApiResponse(responseCode = "404", description = "Shipment no encontrado"),
            @ApiResponse(responseCode = "409", description = "Transición de estado inválida")
    })
    public ResponseEntity<Void> registerEvent(
            @PathVariable UUID shipmentId,
            @RequestBody RegisterEventRequest request) {
        String correlationId = UUID.randomUUID().toString();

        TrackingEventRequest eventRequest = new TrackingEventRequest(
                shipmentId,
                request.eventType(),
                request.location(),
                request.occurredAt()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.LOGISTICS_EXCHANGE,
                "tracking.event.request",
                eventRequest,
                message -> {
                    message.getMessageProperties().setHeader("x-correlation-id", correlationId);
                    return message;
                }
        );

        log.info("Accepted async tracking request correlationId={} shipmentId={} eventType={} location={}",
                correlationId,
                shipmentId,
                request.eventType(),
                request.location());

        return ResponseEntity.accepted()
                .header("X-Correlation-Id", correlationId)
                .build();
    }

    @GetMapping("/{shipmentId}/history")
    @Operation(summary = "Obtener historial de eventos", description = "Retorna todos los eventos de tracking para un envío ordenados por fecha")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial obtenido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Shipment no encontrado")
    })
    public ResponseEntity<List<TrackingEventResponse>> getHistory(@PathVariable UUID shipmentId) {
        List<TrackingEvent> events = trackingUseCase.getHistory(shipmentId);
        return ResponseEntity.ok(events.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/{shipmentId}/current")
    @Operation(summary = "Obtener estado actual", description = "Retorna el estado actual de un envío")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado obtenido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Shipment no encontrado")
    })
    public ResponseEntity<ShipmentStatusResponse> getCurrentStatus(@PathVariable UUID shipmentId) {
        Shipment shipment = trackingUseCase.getCurrentStatus(shipmentId);
        return ResponseEntity.ok(new ShipmentStatusResponse(shipment.getStatus().name()));
    }

    private TrackingEventResponse toResponse(TrackingEvent event) {
        return new TrackingEventResponse(
                event.getId(),
                event.getEventType(),
                event.getStatusBefore().name(),
                event.getStatusAfter().name(),
                event.getLocation(),
                event.getOccurredAt()
        );
    }

    record RegisterEventRequest(String eventType, String location, LocalDateTime occurredAt) {}
    record TrackingEventResponse(UUID id, String eventType, String statusBefore,
                                 String statusAfter, String location, LocalDateTime occurredAt) {}
    record ShipmentStatusResponse(String status) {}
}
