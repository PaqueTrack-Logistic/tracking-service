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
    public static final String TRACKING_EVENT_REQUEST_QUEUE = "tracking.event.request.queue";
    public static final String TRACKING_EVENT_REQUEST_DLX = "tracking.event.request.dlx";
    public static final String TRACKING_EVENT_REQUEST_DLQ = "tracking.event.request.dlq";


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

    @Bean
    public Queue trackingEventRequestQueue() {
        return QueueBuilder.durable(TRACKING_EVENT_REQUEST_QUEUE)
                .withArgument("x-dead-letter-exchange", TRACKING_EVENT_REQUEST_DLX)
                .withArgument("x-dead-letter-routing-key", TRACKING_EVENT_REQUEST_DLQ)
                .build();
    }

    @Bean
    public TopicExchange trackingEventRequestDeadLetterExchange() {
        return new TopicExchange(TRACKING_EVENT_REQUEST_DLX);
    }

    @Bean
    public Queue trackingEventRequestDeadLetterQueue() {
        return new Queue(TRACKING_EVENT_REQUEST_DLQ, true);
    }

    @Bean
    public Binding trackingEventRequestBinding() {
        return BindingBuilder.bind(trackingEventRequestQueue())
                .to(logisticsExchange())
                .with("tracking.event.request");
    }

    @Bean
    public Binding trackingEventRequestDeadLetterBinding() {
        return BindingBuilder.bind(trackingEventRequestDeadLetterQueue())
                .to(trackingEventRequestDeadLetterExchange())
                .with(TRACKING_EVENT_REQUEST_DLQ);
    }
}
