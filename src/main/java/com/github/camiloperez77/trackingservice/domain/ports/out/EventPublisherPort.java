package com.github.camiloperez77.trackingservice.domain.ports.out;

import com.github.camiloperez77.trackingservice.domain.model.TrackingEventNotification;

public interface EventPublisherPort {
    void publishTrackingEventRecorded(TrackingEventNotification notification);
}