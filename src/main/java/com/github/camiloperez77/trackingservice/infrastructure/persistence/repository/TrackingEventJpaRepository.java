package com.github.camiloperez77.trackingservice.infrastructure.persistence.repository;


import com.github.camiloperez77.trackingservice.domain.model.EventType;
import com.github.camiloperez77.trackingservice.infrastructure.persistence.entity.TrackingEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TrackingEventJpaRepository extends JpaRepository<TrackingEventEntity, UUID> {
    Page<TrackingEventEntity> findByShipmentId(UUID shipmentId, Pageable pageable);
    Optional<TrackingEventEntity> findFirstByShipmentIdOrderByOccurredAtAsc(UUID shipmentId);
    Optional<TrackingEventEntity> findFirstByShipmentIdAndEventTypeOrderByOccurredAtDesc(UUID shipmentId, EventType eventType);
    Optional<TrackingEventEntity> findFirstByShipmentIdOrderByOccurredAtDesc(UUID shipmentId);
    Optional<TrackingEventEntity> findLastByShipmentIdOrderByOccurredAtDesc(UUID shipmentId);
}