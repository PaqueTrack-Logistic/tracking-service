package com.github.camiloperez77.trackingservice.infrastructure.persistence.mapper;

import com.github.camiloperez77.trackingservice.domain.model.ShipmentStatus;
import com.github.camiloperez77.trackingservice.domain.model.TrackingEvent;
import com.github.camiloperez77.trackingservice.infrastructure.persistence.entity.TrackingEventEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TrackingEventMapperTest {

    private final TrackingEventMapper mapper = new TrackingEventMapper();

    @Test
    @DisplayName("toDomain maps all fields")
    void toDomain_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        UUID shipmentId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.now().minusHours(1);
        LocalDateTime createdAt = LocalDateTime.now();

        TrackingEventEntity entity = new TrackingEventEntity();
        entity.setId(id);
        entity.setShipmentId(shipmentId);
        entity.setEventType("DISPATCHED");
        entity.setStatusBefore("CREATED");
        entity.setStatusAfter("IN_TRANSIT");
        entity.setLocation("Hub Central");
        entity.setOccurredAt(occurredAt);
        entity.setCreatedAt(createdAt);

        TrackingEvent domain = mapper.toDomain(entity);

        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getShipmentId()).isEqualTo(shipmentId);
        assertThat(domain.getEventType()).isEqualTo("DISPATCHED");
        assertThat(domain.getStatusBefore()).isEqualTo(ShipmentStatus.CREATED);
        assertThat(domain.getStatusAfter()).isEqualTo(ShipmentStatus.IN_TRANSIT);
        assertThat(domain.getLocation()).isEqualTo("Hub Central");
        assertThat(domain.getOccurredAt()).isEqualTo(occurredAt);
        assertThat(domain.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("toEntity maps all fields")
    void toEntity_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        UUID shipmentId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.now().minusHours(1);
        LocalDateTime createdAt = LocalDateTime.now();

        TrackingEvent domain = new TrackingEvent(id, shipmentId, "DELIVERED",
                ShipmentStatus.OUT_FOR_DELIVERY, ShipmentStatus.DELIVERED,
                "Destination", occurredAt, createdAt);

        TrackingEventEntity entity = mapper.toEntity(domain);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getShipmentId()).isEqualTo(shipmentId);
        assertThat(entity.getEventType()).isEqualTo("DELIVERED");
        assertThat(entity.getStatusBefore()).isEqualTo("OUT_FOR_DELIVERY");
        assertThat(entity.getStatusAfter()).isEqualTo("DELIVERED");
        assertThat(entity.getLocation()).isEqualTo("Destination");
        assertThat(entity.getOccurredAt()).isEqualTo(occurredAt);
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("toDomain handles null statusBefore")
    void toDomain_nullStatusBefore_shouldMapToNull() {
        TrackingEventEntity entity = new TrackingEventEntity();
        entity.setId(UUID.randomUUID());
        entity.setShipmentId(UUID.randomUUID());
        entity.setEventType("DISPATCHED");
        entity.setStatusBefore(null);
        entity.setStatusAfter("IN_TRANSIT");
        entity.setLocation("Hub");
        entity.setOccurredAt(LocalDateTime.now());
        entity.setCreatedAt(LocalDateTime.now());

        TrackingEvent domain = mapper.toDomain(entity);

        assertThat(domain.getStatusBefore()).isNull();
        assertThat(domain.getStatusAfter()).isEqualTo(ShipmentStatus.IN_TRANSIT);
    }

    @Test
    @DisplayName("toDomain handles null statusAfter")
    void toDomain_nullStatusAfter_shouldMapToNull() {
        TrackingEventEntity entity = new TrackingEventEntity();
        entity.setId(UUID.randomUUID());
        entity.setShipmentId(UUID.randomUUID());
        entity.setEventType("UNKNOWN");
        entity.setStatusBefore("CREATED");
        entity.setStatusAfter(null);
        entity.setLocation("Hub");
        entity.setOccurredAt(LocalDateTime.now());
        entity.setCreatedAt(LocalDateTime.now());

        TrackingEvent domain = mapper.toDomain(entity);

        assertThat(domain.getStatusBefore()).isEqualTo(ShipmentStatus.CREATED);
        assertThat(domain.getStatusAfter()).isNull();
    }

    @Test
    @DisplayName("toEntity handles null statusBefore")
    void toEntity_nullStatusBefore_shouldMapToNull() {
        TrackingEvent domain = new TrackingEvent(UUID.randomUUID(), UUID.randomUUID(), "DISPATCHED",
                null, ShipmentStatus.IN_TRANSIT, "Hub", LocalDateTime.now(), LocalDateTime.now());

        TrackingEventEntity entity = mapper.toEntity(domain);

        assertThat(entity.getStatusBefore()).isNull();
        assertThat(entity.getStatusAfter()).isEqualTo("IN_TRANSIT");
    }

    @Test
    @DisplayName("toEntity handles null statusAfter")
    void toEntity_nullStatusAfter_shouldMapToNull() {
        TrackingEvent domain = new TrackingEvent(UUID.randomUUID(), UUID.randomUUID(), "UNKNOWN",
                ShipmentStatus.CREATED, null, "Hub", LocalDateTime.now(), LocalDateTime.now());

        TrackingEventEntity entity = mapper.toEntity(domain);

        assertThat(entity.getStatusBefore()).isEqualTo("CREATED");
        assertThat(entity.getStatusAfter()).isNull();
    }

    @Test
    @DisplayName("Roundtrip: toDomain(toEntity(domain)) preserves data")
    void roundtrip_shouldPreserveData() {
        UUID id = UUID.randomUUID();
        UUID shipmentId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.now().minusHours(1);
        LocalDateTime createdAt = LocalDateTime.now();

        TrackingEvent original = new TrackingEvent(id, shipmentId, "OUT_FOR_DELIVERY",
                ShipmentStatus.IN_TRANSIT, ShipmentStatus.OUT_FOR_DELIVERY,
                "Distribution Center", occurredAt, createdAt);

        TrackingEvent result = mapper.toDomain(mapper.toEntity(original));

        assertThat(result.getId()).isEqualTo(original.getId());
        assertThat(result.getShipmentId()).isEqualTo(original.getShipmentId());
        assertThat(result.getEventType()).isEqualTo(original.getEventType());
        assertThat(result.getStatusBefore()).isEqualTo(original.getStatusBefore());
        assertThat(result.getStatusAfter()).isEqualTo(original.getStatusAfter());
        assertThat(result.getLocation()).isEqualTo(original.getLocation());
        assertThat(result.getOccurredAt()).isEqualTo(original.getOccurredAt());
        assertThat(result.getCreatedAt()).isEqualTo(original.getCreatedAt());
    }
}
