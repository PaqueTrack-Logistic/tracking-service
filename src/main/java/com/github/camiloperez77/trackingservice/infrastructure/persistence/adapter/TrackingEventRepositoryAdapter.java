package com.github.camiloperez77.trackingservice.infrastructure.persistence.adapter;

import com.github.camiloperez77.trackingservice.domain.model.TrackingEvent;
import com.github.camiloperez77.trackingservice.domain.ports.out.TrackingEventRepositoryPort;
import com.github.camiloperez77.trackingservice.infrastructure.persistence.entity.TrackingEventEntity;
import com.github.camiloperez77.trackingservice.infrastructure.persistence.mapper.TrackingEventMapper;
import com.github.camiloperez77.trackingservice.infrastructure.persistence.repository.TrackingEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public List<TrackingEvent> findByShipmentIdOrderByOccurredAtAsc(UUID shipmentId) {
        List<TrackingEventEntity> entities = jpaRepository.findByShipmentIdOrderByOccurredAtAsc(shipmentId);
        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
