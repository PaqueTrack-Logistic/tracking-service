package com.github.camiloperez77.trackingservice.infrastructure.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String LOGISTICS_EXCHANGE = "logistics.exchange";
    public static final String SHIPMENT_CREATED_QUEUE = "tracking.shipment.created.queue";
    public static final String TRACKING_EVENT_RECORDED_QUEUE = "tracking.event.recorded.queue";

    @Bean
    public Queue shipmentCreatedQueue() {
        return new Queue(SHIPMENT_CREATED_QUEUE, true);
    }

    @Bean
    public Queue trackingEventRecordedQueue() {
        return new Queue(TRACKING_EVENT_RECORDED_QUEUE, true);
    }

    @Bean
    public TopicExchange logisticsExchange() {
        return new TopicExchange(LOGISTICS_EXCHANGE);
    }

    @Bean
    public Binding shipmentCreatedBinding() {
        return BindingBuilder.bind(shipmentCreatedQueue())
                .to(logisticsExchange())
                .with("shipment.created");
    }

    @Bean
    public Binding trackingEventRecordedBinding() {
        return BindingBuilder.bind(trackingEventRecordedQueue())
                .to(logisticsExchange())
                .with("tracking.event.recorded");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}