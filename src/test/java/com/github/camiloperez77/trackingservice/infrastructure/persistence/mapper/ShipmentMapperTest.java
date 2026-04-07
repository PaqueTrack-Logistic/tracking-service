package com.github.camiloperez77.trackingservice.infrastructure.persistence.mapper;

import com.github.camiloperez77.trackingservice.domain.model.Shipment;
import com.github.camiloperez77.trackingservice.domain.model.ShipmentStatus;
import com.github.camiloperez77.trackingservice.infrastructure.persistence.entity.ShipmentEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ShipmentMapperTest {

    private final ShipmentMapper mapper = new ShipmentMapper();

    @Test
    @DisplayName("toDomain maps all fields including status enum conversion")
    void toDomain_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        ShipmentEntity entity = new ShipmentEntity();
        entity.setId(id);
        entity.setTrackingId("TRK-100");
        entity.setStatus("IN_TRANSIT");
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);

        Shipment domain = mapper.toDomain(entity);

        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getTrackingId()).isEqualTo("TRK-100");
        assertThat(domain.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
        assertThat(domain.getCreatedAt()).isEqualTo(createdAt);
        assertThat(domain.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("toEntity maps all fields")
    void toEntity_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        Shipment domain = new Shipment(id, "TRK-200", ShipmentStatus.DELIVERED, createdAt, updatedAt);

        ShipmentEntity entity = mapper.toEntity(domain);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getTrackingId()).isEqualTo("TRK-200");
        assertThat(entity.getStatus()).isEqualTo("DELIVERED");
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("toDomain correctly converts each ShipmentStatus enum value")
    void toDomain_shouldConvertAllStatusValues() {
        ShipmentEntity entity = new ShipmentEntity();
        entity.setId(UUID.randomUUID());
        entity.setTrackingId("TRK-300");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        for (ShipmentStatus status : ShipmentStatus.values()) {
            entity.setStatus(status.name());
            Shipment domain = mapper.toDomain(entity);
            assertThat(domain.getStatus()).isEqualTo(status);
        }
    }

    @Test
    @DisplayName("Roundtrip: toDomain(toEntity(domain)) preserves data")
    void roundtrip_shouldPreserveData() {
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        Shipment original = new Shipment(id, "TRK-400", ShipmentStatus.EXCEPTION, createdAt, updatedAt);

        Shipment result = mapper.toDomain(mapper.toEntity(original));

        assertThat(result.getId()).isEqualTo(original.getId());
        assertThat(result.getTrackingId()).isEqualTo(original.getTrackingId());
        assertThat(result.getStatus()).isEqualTo(original.getStatus());
        assertThat(result.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(result.getUpdatedAt()).isEqualTo(original.getUpdatedAt());
    }
}
