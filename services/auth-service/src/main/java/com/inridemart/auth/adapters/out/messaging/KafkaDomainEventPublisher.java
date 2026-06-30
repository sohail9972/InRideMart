package com.inridemart.auth.adapters.out.messaging;

import com.inridemart.auth.domain.UserRegisteredEvent;
import com.inridemart.auth.domain.ports.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaDomainEventPublisher implements DomainEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(KafkaDomainEventPublisher.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaDomainEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(UserRegisteredEvent event) {
        kafkaTemplate.send("auth.user.registered.v1", event.userId().toString(), event)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.warn("Failed to publish user registered event for {}", event.userId(), error);
                    }
                });
    }
}

