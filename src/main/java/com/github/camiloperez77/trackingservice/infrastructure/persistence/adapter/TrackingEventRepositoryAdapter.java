package com.github.camiloperez77.trackingservice.infrastructure.persistence.adapter;

import com.github.camiloperez77.trackingservice.domain.model.TrackingEvent;
import com.github.camiloperez77.trackingservice.domain.ports.out.TrackingEventRepositoryPort;
import com.github.camiloperez77.trackingservice.infrastructure.persistence.entity.TrackingEventEntity;
import com.github.camiloperez77.trackingservice.infrastructure.persistence.mapper.TrackingEventMapper;
import com.github.camiloperez77.trackingservice.infrastructure.persistence.repository.TrackingEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TrackingEventRepositoryAdapter implements TrackingEventRepositoryPort {

    private final TrackingEventJpaRepository jpaRepository;
    private final TrackingEventMapper mapper;

    @Override
    public TrackingEvent save(TrackingEvent event) {
        TrackingEventEntity entity = mapper.toEntity(event);
        TrackingEventEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Page<TrackingEvent> findByShipmentIdOrderByOccurredAtAsc(UUID shipmentId, Pageable pageable) {
        Page<TrackingEventEntity> entityPage = jpaRepository.findByShipmentId(shipmentId, pageable);
        return entityPage.map(mapper::toDomain);
    }

    @Override
    public Optional<TrackingEvent> findFirstEventByShipmentIdOrderByOccurredAtAsc(UUID shipmentId) {
        return jpaRepository.findFirstByShipmentIdOrderByOccurredAtAsc(shipmentId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<TrackingEvent> findLastDeliveredEventByShipmentId(UUID shipmentId) {
        return jpaRepository.findFirstByShipmentIdAndEventTypeOrderByOccurredAtDesc(shipmentId, "DELIVERED")
                .map(mapper::toDomain);
    }

    @Override
    public List<TrackingEvent> findLastEventByShipmentIdGrouped() {
        return List.of();
    }

    @Override
    public Optional<TrackingEvent> findLastEventByShipmentId(UUID shipmentId) {
        return jpaRepository.findFirstByShipmentIdOrderByOccurredAtDesc(shipmentId)
                .map(mapper::toDomain);
    }
}
