package com.github.camiloperez77.trackingservice.infrastructure.persistence.repository;


import com.github.camiloperez77.trackingservice.infrastructure.persistence.entity.TrackingEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TrackingEventJpaRepository extends JpaRepository<TrackingEventEntity, UUID> {
    Page<TrackingEventEntity> findByShipmentId(UUID shipmentId, Pageable pageable);
}