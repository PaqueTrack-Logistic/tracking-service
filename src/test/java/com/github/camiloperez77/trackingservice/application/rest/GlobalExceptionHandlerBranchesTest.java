package com.github.camiloperez77.trackingservice.application.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.github.camiloperez77.trackingservice.domain.exception.ShipmentNotFoundException;

/**
 * Cubre los manejadores no ejercitados por el test base (MockMvc):
 * shipmentNotFound (404), validación (400) y la rama genérica/null de
 * IllegalArgumentException (400).
 */
class GlobalExceptionHandlerBranchesTest {

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	@Test
	void handleShipmentNotFound_returns404() {
		ShipmentNotFoundException ex = mock(ShipmentNotFoundException.class);
		when(ex.getMessage()).thenReturn("Shipment no encontrado");

		ResponseEntity<?> response = handler.handleShipmentNotFound(ex);

		assertThat(response.getStatusCode().value()).isEqualTo(404);
	}

	@Test
	void handleIllegalArgument_genericMessage_returns400() {
		ResponseEntity<?> response = handler.handleIllegalArgument(new IllegalArgumentException("parámetro raro"));

		assertThat(response.getStatusCode().value()).isEqualTo(400);
	}

	@Test
	void handleIllegalArgument_nullMessage_returns400() {
		ResponseEntity<?> response = handler.handleIllegalArgument(new IllegalArgumentException());

		assertThat(response.getStatusCode().value()).isEqualTo(400);
	}

	@Test
	void handleValidationExceptions_returns400WithFieldErrors() {
		MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
		BindingResult bindingResult = mock(BindingResult.class);
		when(ex.getBindingResult()).thenReturn(bindingResult);
		when(bindingResult.getFieldErrors())
				.thenReturn(List.of(new FieldError("event", "eventType", "no debe ser nulo")));

		ResponseEntity<Map<String, String>> response = handler.handleValidationExceptions(ex);

		assertThat(response.getStatusCode().value()).isEqualTo(400);
		assertThat(response.getBody()).containsEntry("eventType", "no debe ser nulo");
	}
}
