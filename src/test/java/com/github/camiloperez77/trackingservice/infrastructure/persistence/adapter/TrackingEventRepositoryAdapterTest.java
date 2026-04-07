package com.github.camiloperez77.trackingservice.infrastructure.persistence.adapter;

import com.github.camiloperez77.trackingservice.domain.model.ShipmentStatus;
import com.github.camiloperez77.trackingservice.domain.model.TrackingEvent;
import com.github.camiloperez77.trackingservice.infrastructure.persistence.entity.TrackingEventEntity;
import com.github.camiloperez77.trackingservice.infrastructure.persistence.mapper.TrackingEventMapper;
import com.github.camiloperez77.trackingservice.infrastructure.persistence.repository.TrackingEventJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackingEventRepositoryAdapterTest {

    @Mock
    private TrackingEventJpaRepository jpaRepository;

    @Mock
    private TrackingEventMapper mapper;

    @InjectMocks
    private TrackingEventRepositoryAdapter adapter;

    private final UUID eventId = UUID.randomUUID();
    private final UUID shipmentId = UUID.randomUUID();
    private final LocalDateTime occurredAt = LocalDateTime.of(2026, 4, 7, 10, 0, 0);
    private final LocalDateTime createdAt = LocalDateTime.of(2026, 4, 7, 10, 0, 1);

    private TrackingEvent buildDomainEvent() {
        return new TrackingEvent(
                eventId, shipmentId, "STATUS_CHANGE",
                ShipmentStatus.CREATED, ShipmentStatus.IN_TRANSIT,
                "Bogota", occurredAt, createdAt
        );
    }

    private TrackingEventEntity buildEntity() {
        TrackingEventEntity entity = new TrackingEventEntity();
        entity.setId(eventId);
        entity.setShipmentId(shipmentId);
        entity.setEventType("STATUS_CHANGE");
        entity.setStatusBefore("CREATED");
        entity.setStatusAfter("IN_TRANSIT");
        entity.setLocation("Bogota");
        entity.setOccurredAt(occurredAt);
        entity.setCreatedAt(createdAt);
        return entity;
    }

    @Test
    void save_shouldMapDomainToEntityAndSaveAndMapBack() {
        TrackingEvent domain = buildDomainEvent();
        TrackingEventEntity entity = buildEntity();
        TrackingEventEntity savedEntity = buildEntity();
        TrackingEvent expectedResult = buildDomainEvent();

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(expectedResult);

        TrackingEvent result = adapter.save(domain);

        assertThat(result).isSameAs(expectedResult);
        verify(mapper).toEntity(domain);
        verify(jpaRepository).save(entity);
        verify(mapper).toDomain(savedEntity);
    }

    @Test
    void findByShipmentIdOrderByOccurredAtAsc_shouldReturnMappedList() {
        TrackingEventEntity entity1 = buildEntity();
        TrackingEventEntity entity2 = buildEntity();
        entity2.setId(UUID.randomUUID());

        TrackingEvent domain1 = buildDomainEvent();
        TrackingEvent domain2 = buildDomainEvent();

        when(jpaRepository.findByShipmentIdOrderByOccurredAtAsc(shipmentId))
                .thenReturn(List.of(entity1, entity2));
        when(mapper.toDomain(entity1)).thenReturn(domain1);
        when(mapper.toDomain(entity2)).thenReturn(domain2);

        List<TrackingEvent> result = adapter.findByShipmentIdOrderByOccurredAtAsc(shipmentId);

        assertThat(result).hasSize(2).containsExactly(domain1, domain2);
        verify(jpaRepository).findByShipmentIdOrderByOccurredAtAsc(shipmentId);
        verify(mapper).toDomain(entity1);
        verify(mapper).toDomain(entity2);
    }
}
