package com.github.camiloperez77.trackingservice.infrastructure.persistence.repository;

import com.github.camiloperez77.trackingservice.infrastructure.persistence.entity.ShipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShipmentJpaRepository extends JpaRepository<ShipmentEntity, UUID> {
	Optional<ShipmentEntity> findByTrackingId(String trackingId);
}