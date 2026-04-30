package com.github.camiloperez77.trackingservice.application.rest;

import com.github.camiloperez77.trackingservice.domain.model.EventType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tracking/eventTypes")
@RequiredArgsConstructor
@Tag(name = "EventType", description = "Catálogo de tipos de eventos")
public class EventTypeController {

    @GetMapping
     @Operation(summary = "Obtener tipos de eventos", description = "Retorna todos los tipos de eventos de tracking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipos de evento obtenidos exitosamente"),
            @ApiResponse(responseCode = "404", description = "Tipo de evento no encontrado")
    })
    public List<Map<String, String>> getEventTypes() {
        return Arrays.stream(EventType.values())
                .map(et -> Map.of(
                    "name", et.name(),
                    "targetStatus", et.getTargetStatus().name()
                ))
                .collect(Collectors.toList());
    }
}
