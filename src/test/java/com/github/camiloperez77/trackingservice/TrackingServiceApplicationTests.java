package com.github.camiloperez77.trackingservice;

import com.github.camiloperez77.trackingservice.infrastructure.messaging.publisher.RabbitMQEventPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TrackingServiceApplicationTests {

    @TestConfiguration
    static class MockConfig {

        @Bean
        RabbitMQEventPublisher rabbitMQEventPublisher() {
            return Mockito.mock(RabbitMQEventPublisher.class);
        }
    }

    @Test
    void contextLoads() {
        // si el contexto carga, este test pasa
    }
}
