package com.github.camiloperez77.trackingservice.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Cubre las ramas de canTransitionTo no ejercitadas por ShipmentStatusTest:
 * AT_TRANSIT_POINT, DELIVERED (estado terminal) y EXCEPTION (recuperación).
 */
class ShipmentStatusBranchesTest {

	@Test
	void atTransitPoint_validTransitions() {
		assertThat(ShipmentStatus.AT_TRANSIT_POINT.canTransitionTo(ShipmentStatus.IN_TRANSIT)).isTrue();
		assertThat(ShipmentStatus.AT_TRANSIT_POINT.canTransitionTo(ShipmentStatus.OUT_FOR_DELIVERY)).isTrue();
		assertThat(ShipmentStatus.AT_TRANSIT_POINT.canTransitionTo(ShipmentStatus.EXCEPTION)).isTrue();
	}

	@Test
	void atTransitPoint_invalidTransitions() {
		assertThat(ShipmentStatus.AT_TRANSIT_POINT.canTransitionTo(ShipmentStatus.CREATED)).isFalse();
		assertThat(ShipmentStatus.AT_TRANSIT_POINT.canTransitionTo(ShipmentStatus.DELIVERED)).isFalse();
		assertThat(ShipmentStatus.AT_TRANSIT_POINT.canTransitionTo(ShipmentStatus.AT_TRANSIT_POINT)).isFalse();
	}

	@Test
	void delivered_isTerminalState() {
		assertThat(ShipmentStatus.DELIVERED.canTransitionTo(ShipmentStatus.IN_TRANSIT)).isFalse();
		assertThat(ShipmentStatus.DELIVERED.canTransitionTo(ShipmentStatus.EXCEPTION)).isFalse();
		assertThat(ShipmentStatus.DELIVERED.canTransitionTo(ShipmentStatus.OUT_FOR_DELIVERY)).isFalse();
	}

	@Test
	void exception_allowsRecovery() {
		assertThat(ShipmentStatus.EXCEPTION.canTransitionTo(ShipmentStatus.CREATED)).isTrue();
		assertThat(ShipmentStatus.EXCEPTION.canTransitionTo(ShipmentStatus.IN_TRANSIT)).isTrue();
		assertThat(ShipmentStatus.EXCEPTION.canTransitionTo(ShipmentStatus.AT_TRANSIT_POINT)).isTrue();
	}

	@Test
	void exception_invalidToDelivered() {
		assertThat(ShipmentStatus.EXCEPTION.canTransitionTo(ShipmentStatus.DELIVERED)).isFalse();
	}

	@Test
	void outForDelivery_invalidToInTransit() {
		assertThat(ShipmentStatus.OUT_FOR_DELIVERY.canTransitionTo(ShipmentStatus.IN_TRANSIT)).isFalse();
	}
}
