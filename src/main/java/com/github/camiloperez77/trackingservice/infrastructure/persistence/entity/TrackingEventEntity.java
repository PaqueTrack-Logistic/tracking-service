package com.github.camiloperez77.trackingservice.infrastructure.persistence.entity;
import com.github.camiloperez77.trackingservice.domain.model.EventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tracking_event")
@Getter
@Setter
public class TrackingEventEntity {
    @Id
    private UUID id;
    private UUID shipmentId;
    
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    private String statusBefore;
    private String statusAfter;
    private String location;
    private LocalDateTime occurredAt;
    private LocalDateTime createdAt;
}