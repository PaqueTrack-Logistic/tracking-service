package com.github.camiloperez77.trackingservice.application.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.camiloperez77.trackingservice.domain.model.EventType;

/**
 * Cubre el catálogo de tipos de evento: devuelve todos los EventType con su
 * estado destino.
 */
class EventTypeControllerTest {

	private final EventTypeController controller = new EventTypeController();

	@Test
	void getEventTypes_returnsAllTypesWithTargetStatus() {
		List<Map<String, String>> result = controller.getEventTypes();

		assertThat(result).hasSize(EventType.values().length);
		assertThat(result).allSatisfy(m -> assertThat(m).containsKeys("name", "targetStatus"));
		assertThat(result).anyMatch(m ->
				"DISPATCHED".equals(m.get("name")) && "IN_TRANSIT".equals(m.get("targetStatus")));
		assertThat(result).anyMatch(m ->
				"DELIVERED".equals(m.get("name")) && "DELIVERED".equals(m.get("targetStatus")));
	}
}
