package com.inridemart.order;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class OrderStatusTest {
    @Test void exposesTheFourStepInRideLifecycle() {
        assertArrayEquals(new OrderStatus[]{
                OrderStatus.PLACED,
                OrderStatus.CONFIRMED,
                OrderStatus.HANDED_OVER,
                OrderStatus.COMPLETED
        }, OrderStatus.values());
    }
}
