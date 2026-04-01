package com.github.camiloperez77.trackingservice.infrastructure.persistence.repository;


import com.github.camiloperez77.trackingservice.infrastructure.persistence.entity.TrackingEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TrackingEventJpaRepository extends JpaRepository<TrackingEventEntity, UUID> {
    List<TrackingEventEntity> findByShipmentIdOrderByOccurredAtAsc(UUID shipmentId);
}