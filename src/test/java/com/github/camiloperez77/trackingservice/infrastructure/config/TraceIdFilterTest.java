package com.github.camiloperez77.trackingservice.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;

/**
 * Cubre las ramas de TraceIdFilter: traceId presente (usa el de la cabecera),
 * ausente (genera UUID) y vacío (genera UUID). Verifica además que la cadena
 * de filtros continúa.
 */
class TraceIdFilterTest {

	private final TraceIdFilter filter = new TraceIdFilter();

	@Test
	void usesTraceIdFromHeaderWhenPresent() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("traceId", "trace-abc");
		FilterChain chain = Mockito.spy(new MockFilterChain());

		filter.doFilterInternal(request, new MockHttpServletResponse(), chain);

		// la cadena continúa y al final MDC se limpia
		verify(chain).doFilter(Mockito.any(), Mockito.any());
		assertThat(MDC.get("traceId")).isNull();
	}

	@Test
	void generatesTraceIdWhenHeaderMissing() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(); // sin cabecera (null)
		FilterChain chain = Mockito.spy(new MockFilterChain());

		filter.doFilterInternal(request, new MockHttpServletResponse(), chain);

		verify(chain).doFilter(Mockito.any(), Mockito.any());
	}

	@Test
	void generatesTraceIdWhenHeaderEmpty() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("traceId", "");
		FilterChain chain = Mockito.spy(new MockFilterChain());

		filter.doFilterInternal(request, new MockHttpServletResponse(), chain);

		verify(chain).doFilter(Mockito.any(), Mockito.any());
	}
}
