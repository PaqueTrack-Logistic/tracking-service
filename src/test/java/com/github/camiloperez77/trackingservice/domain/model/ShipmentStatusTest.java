package com.github.camiloperez77.trackingservice.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShipmentStatusTest {

    @Nested
    @DisplayName("CREATED transitions")
    class CreatedTransitions {

        @Test
        @DisplayName("CREATED -> IN_TRANSIT is valid")
        void createdToInTransit_shouldBeValid() {
            assertThat(ShipmentStatus.CREATED.canTransitionTo(ShipmentStatus.IN_TRANSIT)).isTrue();
        }

        @Test
        @DisplayName("CP-04-02: CREATED -> DELIVERED is invalid")
        void createdToDelivered_shouldBeInvalid() {
            assertThat(ShipmentStatus.CREATED.canTransitionTo(ShipmentStatus.DELIVERED)).isFalse();
        }

        @Test
        @DisplayName("CREATED -> OUT_FOR_DELIVERY is invalid")
        void createdToOutForDelivery_shouldBeInvalid() {
            assertThat(ShipmentStatus.CREATED.canTransitionTo(ShipmentStatus.OUT_FOR_DELIVERY)).isFalse();
        }

        @Test
        @DisplayName("CREATED -> EXCEPTION is invalid")
        void createdToException_shouldBeInvalid() {
            assertThat(ShipmentStatus.CREATED.canTransitionTo(ShipmentStatus.EXCEPTION)).isFalse();
        }

        @Test
        @DisplayName("CREATED -> CREATED is invalid")
        void createdToCreated_shouldBeInvalid() {
            assertThat(ShipmentStatus.CREATED.canTransitionTo(ShipmentStatus.CREATED)).isFalse();
        }
    }

    @Nested
    @DisplayName("IN_TRANSIT transitions")
    class InTransitTransitions {

        @Test
        @DisplayName("IN_TRANSIT -> OUT_FOR_DELIVERY is valid")
        void inTransitToOutForDelivery_shouldBeValid() {
            assertThat(ShipmentStatus.IN_TRANSIT.canTransitionTo(ShipmentStatus.OUT_FOR_DELIVERY)).isTrue();
        }

        @Test
        @DisplayName("IN_TRANSIT -> EXCEPTION is valid")
        void inTransitToException_shouldBeValid() {
            assertThat(ShipmentStatus.IN_TRANSIT.canTransitionTo(ShipmentStatus.EXCEPTION)).isTrue();
        }

        @Test
        @DisplayName("IN_TRANSIT -> DELIVERED is invalid")
        void inTransitToDelivered_shouldBeInvalid() {
            assertThat(ShipmentStatus.IN_TRANSIT.canTransitionTo(ShipmentStatus.DELIVERED)).isFalse();
        }

        @Test
        @DisplayName("IN_TRANSIT -> CREATED is invalid")
        void inTransitToCreated_shouldBeInvalid() {
            assertThat(ShipmentStatus.IN_TRANSIT.canTransitionTo(ShipmentStatus.CREATED)).isFalse();
        }
    }

    @Nested
    @DisplayName("OUT_FOR_DELIVERY transitions")
    class OutForDeliveryTransitions {

        @Test
        @DisplayName("OUT_FOR_DELIVERY -> DELIVERED is valid")
        void outForDeliveryToDelivered_shouldBeValid() {
            assertThat(ShipmentStatus.OUT_FOR_DELIVERY.canTransitionTo(ShipmentStatus.DELIVERED)).isTrue();
        }

        @Test
        @DisplayName("OUT_FOR_DELIVERY -> EXCEPTION is valid")
        void outForDeliveryToException_shouldBeValid() {
            assertThat(ShipmentStatus.OUT_FOR_DELIVERY.canTransitionTo(ShipmentStatus.EXCEPTION)).isTrue();
        }

        @Test
        @DisplayName("OUT_FOR_DELIVERY -> IN_TRANSIT is invalid")
        void outForDeliveryToInTransit_shouldBeInvalid() {
            assertThat(ShipmentStatus.OUT_FOR_DELIVERY.canTransitionTo(ShipmentStatus.IN_TRANSIT)).isFalse();
        }

        @Test
        @DisplayName("OUT_FOR_DELIVERY -> CREATED is invalid")
        void outForDeliveryToCreated_shouldBeInvalid() {
            assertThat(ShipmentStatus.OUT_FOR_DELIVERY.canTransitionTo(ShipmentStatus.CREATED)).isFalse();
        }
    }

    @Nested
    @DisplayName("DELIVERED transitions (terminal state)")
    class DeliveredTransitions {

        @Test
        @DisplayName("DELIVERED -> any is invalid (terminal state)")
        void delivered_shouldNotTransitionToAnyState() {
            for (ShipmentStatus status : ShipmentStatus.values()) {
                assertThat(ShipmentStatus.DELIVERED.canTransitionTo(status))
                        .as("DELIVERED -> %s should be invalid", status)
                        .isFalse();
            }
        }
    }

    @Nested
    @DisplayName("EXCEPTION transitions (recovery)")
    class ExceptionTransitions {

        @Test
        @DisplayName("EXCEPTION -> IN_TRANSIT is valid (recovery)")
        void exceptionToInTransit_shouldBeValid() {
            assertThat(ShipmentStatus.EXCEPTION.canTransitionTo(ShipmentStatus.IN_TRANSIT)).isTrue();
        }

        @Test
        @DisplayName("EXCEPTION -> CREATED is valid")
        void exceptionToCreated_shouldBeValid() {
            assertThat(ShipmentStatus.EXCEPTION.canTransitionTo(ShipmentStatus.CREATED)).isTrue();
        }

        @Test
        @DisplayName("EXCEPTION -> DELIVERED is invalid")
        void exceptionToDelivered_shouldBeInvalid() {
            assertThat(ShipmentStatus.EXCEPTION.canTransitionTo(ShipmentStatus.DELIVERED)).isFalse();
        }

        @Test
        @DisplayName("EXCEPTION -> OUT_FOR_DELIVERY is invalid")
        void exceptionToOutForDelivery_shouldBeInvalid() {
            assertThat(ShipmentStatus.EXCEPTION.canTransitionTo(ShipmentStatus.OUT_FOR_DELIVERY)).isFalse();
        }
    }
}
