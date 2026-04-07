package com.github.camiloperez77.trackingservice.karate;

import com.intuit.karate.junit5.Karate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TrackingKarateTest {

    @LocalServerPort
    private int port;

    @Karate.Test
    Karate testTracking() {
        return Karate.run("classpath:karate/tracking/tracking.feature")
                .systemProperty("local.server.port", String.valueOf(port));
    }
}
