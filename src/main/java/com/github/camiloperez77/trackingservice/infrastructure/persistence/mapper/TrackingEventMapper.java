package com.github.camiloperez77.trackingservice.infrastructure.persistence.mapper;

import com.github.camiloperez77.trackingservice.domain.model.TrackingEvent;
import com.github.camiloperez77.trackingservice.domain.model.ShipmentStatus;
import com.github.camiloperez77.trackingservice.infrastructure.persistence.entity.TrackingEventEntity;
import org.springframework.stereotype.Component;

@Component
public class TrackingEventMapper {

    public TrackingEventEntity toEntity(TrackingEvent domain) {
        TrackingEventEntity entity = new TrackingEventEntity();
        entity.setId(domain.getId());
        entity.setShipmentId(domain.getShipmentId());
        entity.setEventType(domain.getEventType());
        entity.setStatusBefore(domain.getStatusBefore() != null ? domain.getStatusBefore().name() : null);
        entity.setStatusAfter(domain.getStatusAfter() != null ? domain.getStatusAfter().name() : null);
        entity.setLocation(domain.getLocation());
        entity.setOccurredAt(domain.getOccurredAt());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    public TrackingEvent toDomain(TrackingEventEntity entity) {
        return new TrackingEvent(
                entity.getId(),
                entity.getShipmentId(),
                entity.getEventType(),
                entity.getStatusBefore() != null ? ShipmentStatus.valueOf(entity.getStatusBefore()) : null,
                entity.getStatusAfter() != null ? ShipmentStatus.valueOf(entity.getStatusAfter()) : null,
                entity.getLocation(),
                entity.getOccurredAt(),
                entity.getCreatedAt()
        );
    }
}
