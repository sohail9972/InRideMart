package com.inridemart.order;
import java.math.BigDecimal; import java.util.UUID;
public record OrderItem(UUID productId, String productName, int quantity, BigDecimal unitPrice, String currency) { public OrderItem { if (quantity < 1) throw new IllegalArgumentException("Quantity must be at least 1"); } public BigDecimal lineTotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); } }
