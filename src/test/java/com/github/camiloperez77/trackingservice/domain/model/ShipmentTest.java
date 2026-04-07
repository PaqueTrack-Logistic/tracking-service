package com.github.camiloperez77.trackingservice.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShipmentTest {

    @Test
    @DisplayName("updateStatus with valid transition changes status")
    void updateStatus_validTransition_shouldChangeStatus() {
        Shipment shipment = new Shipment(UUID.randomUUID(), "TRK-001", ShipmentStatus.CREATED);

        shipment.updateStatus(ShipmentStatus.IN_TRANSIT);

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
    }

    @Test
    @DisplayName("updateStatus with invalid transition throws IllegalStateException")
    void updateStatus_invalidTransition_shouldThrowIllegalStateException() {
        Shipment shipment = new Shipment(UUID.randomUUID(), "TRK-001", ShipmentStatus.CREATED);

        assertThatThrownBy(() -> shipment.updateStatus(ShipmentStatus.DELIVERED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid status transition from CREATED to DELIVERED");
    }

    @Test
    @DisplayName("updateStatus updates the updatedAt timestamp")
    void updateStatus_shouldUpdateTimestamp() {
        LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);
        Shipment shipment = new Shipment(UUID.randomUUID(), "TRK-001", ShipmentStatus.CREATED,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().minusHours(1));

        LocalDateTime originalUpdatedAt = shipment.getUpdatedAt();

        shipment.updateStatus(ShipmentStatus.IN_TRANSIT);

        assertThat(shipment.getUpdatedAt()).isAfter(originalUpdatedAt);
        assertThat(shipment.getUpdatedAt()).isAfter(beforeUpdate);
    }

    @Test
    @DisplayName("Constructor sets all fields correctly")
    void constructor_shouldSetAllFields() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Shipment shipment = new Shipment(id, "TRK-002", ShipmentStatus.IN_TRANSIT, now, now);

        assertThat(shipment.getId()).isEqualTo(id);
        assertThat(shipment.getTrackingId()).isEqualTo("TRK-002");
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
        assertThat(shipment.getCreatedAt()).isEqualTo(now);
        assertThat(shipment.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("DELIVERED is a terminal state - cannot transition further")
    void updateStatus_fromDelivered_shouldThrow() {
        Shipment shipment = new Shipment(UUID.randomUUID(), "TRK-001", ShipmentStatus.DELIVERED,
                LocalDateTime.now(), LocalDateTime.now());

        assertThatThrownBy(() -> shipment.updateStatus(ShipmentStatus.CREATED))
                .isInstanceOf(IllegalStateException.class);
    }
}
