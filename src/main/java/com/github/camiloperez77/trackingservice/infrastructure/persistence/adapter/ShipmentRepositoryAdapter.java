package com.github.camiloperez77.trackingservice.infrastructure.persistence.adapter;

import com.github.camiloperez77.trackingservice.domain.model.Shipment;
import com.github.camiloperez77.trackingservice.domain.model.ShipmentStatus;
import com.github.camiloperez77.trackingservice.domain.ports.out.ShipmentRepositoryPort;
import com.github.camiloperez77.trackingservice.infrastructure.persistence.entity.ShipmentEntity;
import com.github.camiloperez77.trackingservice.infrastructure.persistence.mapper.ShipmentMapper;
import com.github.camiloperez77.trackingservice.infrastructure.persistence.repository.ShipmentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ShipmentRepositoryAdapter implements ShipmentRepositoryPort {

    private final ShipmentJpaRepository jpaRepository;
    private final ShipmentMapper mapper;

    @Override
    public Shipment save(Shipment shipment) {
        ShipmentEntity entity = mapper.toEntity(shipment);
        ShipmentEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Shipment> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Shipment> findByTrackingId(String trackingId) {
        return jpaRepository.findByTrackingId(trackingId).map(mapper::toDomain);
    }

    @Override
    public List<Shipment> findByStatusIn(List<ShipmentStatus> statuses) {
        List<String> statusNames = statuses.stream()
            .map(Enum::name)
            .collect(Collectors.toList());
        return jpaRepository.findByStatusIn(statusNames).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
}
