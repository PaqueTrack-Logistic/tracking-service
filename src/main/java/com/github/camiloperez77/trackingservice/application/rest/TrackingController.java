package com.github.camiloperez77.trackingservice.application.rest;

import com.github.camiloperez77.trackingservice.domain.model.Shipment;
import com.github.camiloperez77.trackingservice.domain.model.TrackingEvent;
import com.github.camiloperez77.trackingservice.domain.ports.in.TrackingUseCase;
import com.github.camiloperez77.trackingservice.infrastructure.messaging.config.RabbitMQConfig;
import com.github.camiloperez77.trackingservice.infrastructure.messaging.dto.TrackingEventRequest;
import com.github.camiloperez77.trackingservice.domain.model.EventType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.time.LocalDateTime;
import java.util.UUID;

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
        public ResponseEntity<Void> registerEvent(@PathVariable UUID shipmentId,
                                                @Valid @RequestBody RegisterEventRequest request) {
        // Validar que eventType sea válido (opcional pero recomendado)
        EventType eventType;
        try {
                eventType = EventType.valueOf(request.eventType().toUpperCase());
        } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(null); // o mejor con error response
        }
        
        String correlationId = UUID.randomUUID().toString();
        TrackingEventRequest eventRequest = new TrackingEventRequest(
                shipmentId,
                eventType.name(), // convertimos a String para el DTO
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
    public ResponseEntity<Page<TrackingEventResponse>> getHistory(
        @PathVariable UUID shipmentId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "occurredAt"));
        Page<TrackingEvent> events = trackingUseCase.getHistory(shipmentId, pageable);
        return ResponseEntity.ok(events.map(this::toResponse));
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
                event.getEventType().name(),
                event.getStatusBefore().name(),
                event.getStatusAfter().name(),
                event.getLocation(),
                event.getOccurredAt()
        );
    }

    public record RegisterEventRequest(
    @NotNull(message = "eventType is required") 
    String eventType,
    
    @NotNull(message = "location is required") 
    String location,
    
    @NotNull(message = "occurredAt is required")
    @PastOrPresent(message = "occurredAt cannot be in the future")
    LocalDateTime occurredAt
) {}
    record TrackingEventResponse(UUID id, String eventType, String statusBefore,
                                 String statusAfter, String location, LocalDateTime occurredAt) {}
    record ShipmentStatusResponse(String status) {}
}
