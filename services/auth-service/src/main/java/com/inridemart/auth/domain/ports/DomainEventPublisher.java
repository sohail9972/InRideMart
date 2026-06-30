package com.inridemart.auth.domain.ports;

import com.inridemart.auth.domain.UserRegisteredEvent;

public interface DomainEventPublisher {
    void publish(UserRegisteredEvent event);
}

