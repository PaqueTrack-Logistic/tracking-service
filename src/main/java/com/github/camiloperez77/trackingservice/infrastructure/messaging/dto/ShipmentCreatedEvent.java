package com.github.camiloperez77.trackingservice.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentCreatedEvent implements Serializable {
    private UUID shipmentId;
    private String trackingId;
    private String senderName;
    private String recipientName;
    // otros datos relevantes
}