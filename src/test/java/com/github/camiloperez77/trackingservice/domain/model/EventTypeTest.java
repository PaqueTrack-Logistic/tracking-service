package com.github.camiloperez77.trackingservice.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EventTypeTest {

    @Test
    void dispatchedShouldMapToInTransit() {
        assertEquals(ShipmentStatus.IN_TRANSIT, EventType.DISPATCHED.getTargetStatus());
    }

    @Test
    void arrivedAtHubShouldMapToAtTransitPoint() {
        assertEquals(ShipmentStatus.AT_TRANSIT_POINT, EventType.ARRIVED_AT_HUB.getTargetStatus());
    }

    @Test
    void departedFromHubShouldMapToInTransit() {
        assertEquals(ShipmentStatus.IN_TRANSIT, EventType.DEPARTED_FROM_HUB.getTargetStatus());
    }

    @Test
    void arrivedAtTerminalShouldMapToAtTransitPoint() {
        assertEquals(ShipmentStatus.AT_TRANSIT_POINT, EventType.ARRIVED_AT_TERMINAL.getTargetStatus());
    }

    @Test
    void departedFromTerminalShouldMapToInTransit() {
        assertEquals(ShipmentStatus.IN_TRANSIT, EventType.DEPARTED_FROM_TERMINAL.getTargetStatus());
    }

    @Test
    void outForDeliveryShouldMapToOutForDelivery() {
        assertEquals(ShipmentStatus.OUT_FOR_DELIVERY, EventType.OUT_FOR_DELIVERY.getTargetStatus());
    }

    @Test
    void deliveredShouldMapToDelivered() {
        assertEquals(ShipmentStatus.DELIVERED, EventType.DELIVERED.getTargetStatus());
    }

    @Test
    void damagedShouldMapToException() {
        assertEquals(ShipmentStatus.EXCEPTION, EventType.DAMAGED.getTargetStatus());
    }

    @Test
    void valueOfShouldReturnExpectedEnum() {
        assertEquals(EventType.DISPATCHED, EventType.valueOf("DISPATCHED"));
    }

    @Test
    void valueOfShouldThrowExceptionForInvalidString() {
        assertThrows(IllegalArgumentException.class, () -> EventType.valueOf("INVALID"));
    }
}