package com.inridemart.order;
import jakarta.persistence.*; import java.math.BigDecimal; import java.util.UUID;
@Embeddable public class OrderItemEntity { UUID productId; String productName; int quantity; BigDecimal unitPrice; String currency; protected OrderItemEntity(){} OrderItemEntity(OrderItem i){productId=i.productId();productName=i.productName();quantity=i.quantity();unitPrice=i.unitPrice();currency=i.currency();} OrderItem toDomain(){return new OrderItem(productId,productName,quantity,unitPrice,currency);} }
