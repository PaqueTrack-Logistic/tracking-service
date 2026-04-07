package com.github.camiloperez77.trackingservice.infrastructure.persistence.adapter;

import com.github.camiloperez77.trackingservice.domain.model.Shipment;
import com.github.camiloperez77.trackingservice.domain.model.ShipmentStatus;
import com.github.camiloperez77.trackingservice.infrastructure.persistence.entity.ShipmentEntity;
import com.github.camiloperez77.trackingservice.infrastructure.persistence.mapper.ShipmentMapper;
import com.github.camiloperez77.trackingservice.infrastructure.persistence.repository.ShipmentJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentRepositoryAdapterTest {

    @Mock
    private ShipmentJpaRepository jpaRepository;

    @Mock
    private ShipmentMapper mapper;

    @InjectMocks
    private ShipmentRepositoryAdapter adapter;

    private final UUID shipmentId = UUID.randomUUID();
    private final String trackingId = "TRK-123456";
    private final LocalDateTime now = LocalDateTime.of(2026, 4, 7, 10, 0, 0);

    private Shipment buildDomainShipment() {
        return new Shipment(shipmentId, trackingId, ShipmentStatus.CREATED, now, now);
    }

    private ShipmentEntity buildShipmentEntity() {
        ShipmentEntity entity = new ShipmentEntity();
        entity.setId(shipmentId);
        entity.setTrackingId(trackingId);
        entity.setStatus("CREATED");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return entity;
    }

    @Test
    void save_shouldMapDomainToEntityAndSaveAndMapBack() {
        Shipment domain = buildDomainShipment();
        ShipmentEntity entity = buildShipmentEntity();
        ShipmentEntity savedEntity = buildShipmentEntity();
        Shipment expectedResult = buildDomainShipment();

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(expectedResult);

        Shipment result = adapter.save(domain);

        assertThat(result).isSameAs(expectedResult);
        verify(mapper).toEntity(domain);
        verify(jpaRepository).save(entity);
        verify(mapper).toDomain(savedEntity);
    }

    @Test
    void findById_shouldReturnMappedDomainWhenFound() {
        ShipmentEntity entity = buildShipmentEntity();
        Shipment expectedDomain = buildDomainShipment();

        when(jpaRepository.findById(shipmentId)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(expectedDomain);

        Optional<Shipment> result = adapter.findById(shipmentId);

        assertThat(result).isPresent().containsSame(expectedDomain);
        verify(jpaRepository).findById(shipmentId);
        verify(mapper).toDomain(entity);
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        when(jpaRepository.findById(shipmentId)).thenReturn(Optional.empty());

        Optional<Shipment> result = adapter.findById(shipmentId);

        assertThat(result).isEmpty();
        verify(jpaRepository).findById(shipmentId);
    }

    @Test
    void findByTrackingId_shouldReturnMappedDomainWhenFound() {
        ShipmentEntity entity = buildShipmentEntity();
        Shipment expectedDomain = buildDomainShipment();

        when(jpaRepository.findByTrackingId(trackingId)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(expectedDomain);

        Optional<Shipment> result = adapter.findByTrackingId(trackingId);

        assertThat(result).isPresent().containsSame(expectedDomain);
        verify(jpaRepository).findByTrackingId(trackingId);
        verify(mapper).toDomain(entity);
    }

    @Test
    void findByTrackingId_shouldReturnEmptyWhenNotFound() {
        when(jpaRepository.findByTrackingId(trackingId)).thenReturn(Optional.empty());

        Optional<Shipment> result = adapter.findByTrackingId(trackingId);

        assertThat(result).isEmpty();
        verify(jpaRepository).findByTrackingId(trackingId);
    }
}
