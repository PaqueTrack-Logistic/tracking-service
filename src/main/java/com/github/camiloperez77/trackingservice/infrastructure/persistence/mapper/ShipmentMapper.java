package com.github.camiloperez77.trackingservice.infrastructure.persistence.mapper;

import com.github.camiloperez77.trackingservice.domain.model.Shipment;
import com.github.camiloperez77.trackingservice.domain.model.ShipmentStatus;
import com.github.camiloperez77.trackingservice.infrastructure.persistence.entity.ShipmentEntity;
import org.springframework.stereotype.Component;

@Component
public class ShipmentMapper {
    public ShipmentEntity toEntity(Shipment domain) {
        ShipmentEntity entity = new ShipmentEntity();
        entity.setId(domain.getId());
        entity.setTrackingId(domain.getTrackingId());
        entity.setStatus(domain.getStatus().name());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    public Shipment toDomain(ShipmentEntity entity) {
        return new Shipment(
                entity.getId(),
                entity.getTrackingId(),
                ShipmentStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}