package com.github.camiloperez77.trackingservice.application.rest;

import com.github.camiloperez77.trackingservice.domain.model.Shipment;
import com.github.camiloperez77.trackingservice.domain.model.ShipmentStatus;
import com.github.camiloperez77.trackingservice.domain.model.TrackingEvent;
import com.github.camiloperez77.trackingservice.domain.ports.in.TrackingUseCase;
import com.github.camiloperez77.trackingservice.infrastructure.messaging.config.RabbitMQConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import org.springframework.amqp.core.MessagePostProcessor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TrackingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TrackingUseCase trackingUseCase;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private TrackingController trackingController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trackingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /{shipmentId}/events returns 202 Accepted with X-Correlation-Id header")
    void registerEvent_shouldReturn202WithCorrelationId() throws Exception {
        UUID shipmentId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/tracking/{shipmentId}/events", shipmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "eventType": "DISPATCHED",
                                    "location": "Hub Central",
                                    "occurredAt": "2026-04-07T10:00:00"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(header().exists("X-Correlation-Id"));

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.LOGISTICS_EXCHANGE),
                eq("tracking.event.request"),
                any(Object.class),
                any(MessagePostProcessor.class)
        );
    }

    @Test
    @DisplayName("GET /{shipmentId}/history returns 200 with event list")
    void getHistory_shouldReturn200WithEvents() throws Exception {
        UUID shipmentId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.of(2026, 4, 7, 10, 0, 0);

        TrackingEvent event = new TrackingEvent(
                UUID.randomUUID(), shipmentId, "DISPATCHED",
                ShipmentStatus.CREATED, ShipmentStatus.IN_TRANSIT,
                "Hub Central", occurredAt, LocalDateTime.now()
        );

        when(trackingUseCase.getHistory(shipmentId)).thenReturn(List.of(event));

        mockMvc.perform(get("/api/v1/tracking/{shipmentId}/history", shipmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].eventType").value("DISPATCHED"))
                .andExpect(jsonPath("$[0].statusBefore").value("CREATED"))
                .andExpect(jsonPath("$[0].statusAfter").value("IN_TRANSIT"))
                .andExpect(jsonPath("$[0].location").value("Hub Central"));
    }

    @Test
    @DisplayName("GET /{shipmentId}/history returns 404 when shipment not found")
    void getHistory_shipmentNotFound_shouldReturn404() throws Exception {
        UUID shipmentId = UUID.randomUUID();
        when(trackingUseCase.getHistory(shipmentId))
                .thenThrow(new IllegalArgumentException("Shipment not found with id: " + shipmentId));

        mockMvc.perform(get("/api/v1/tracking/{shipmentId}/history", shipmentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /{shipmentId}/current returns 200 with status")
    void getCurrentStatus_shouldReturn200WithStatus() throws Exception {
        UUID shipmentId = UUID.randomUUID();
        Shipment shipment = new Shipment(shipmentId, "TRK-001", ShipmentStatus.IN_TRANSIT);
        when(trackingUseCase.getCurrentStatus(shipmentId)).thenReturn(shipment);

        mockMvc.perform(get("/api/v1/tracking/{shipmentId}/current", shipmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_TRANSIT"));
    }

    @Test
    @DisplayName("GET /{shipmentId}/current returns 404 when shipment not found")
    void getCurrentStatus_shipmentNotFound_shouldReturn404() throws Exception {
        UUID shipmentId = UUID.randomUUID();
        when(trackingUseCase.getCurrentStatus(shipmentId))
                .thenThrow(new IllegalArgumentException("Shipment not found with id: " + shipmentId));

        mockMvc.perform(get("/api/v1/tracking/{shipmentId}/current", shipmentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
