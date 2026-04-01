package com.github.camiloperez77.trackingservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shipment")
@Getter
@Setter
public class ShipmentEntity {
    @Id
    private UUID id;
    private String trackingId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
