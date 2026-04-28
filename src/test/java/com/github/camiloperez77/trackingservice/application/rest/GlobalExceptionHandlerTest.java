package com.github.camiloperez77.trackingservice.application.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @RestController
    static class FakeController {

        @GetMapping("/fake/not-found")
        public void throwIllegalArgument() {
            throw new IllegalArgumentException("Shipment not found with id: abc-123");
        }

        @GetMapping("/fake/conflict")
        public void throwIllegalState() {
            throw new IllegalStateException("Invalid status transition from CREATED to DELIVERED");
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new FakeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("IllegalArgumentException returns 404 SHIPMENT_NOT_FOUND")
    void handleIllegalArgument_returns404() throws Exception {
        mockMvc.perform(get("/fake/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SHIPMENT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Shipment not found with id: abc-123"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("IllegalStateException returns 409 CONFLICT")
    void handleIllegalState_returns409() throws Exception {
        mockMvc.perform(get("/fake/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATE_TRANSITION"))
                .andExpect(jsonPath("$.message").value("Invalid status transition from CREATED to DELIVERED"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
