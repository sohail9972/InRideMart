package com.inridemart.payment;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="payments") class PaymentEntity { @Id UUID id; @Column(name="order_id",nullable=false,unique=true) UUID orderId; @Column(name="user_id",nullable=false) UUID userId; @Enumerated(EnumType.STRING) @Column(nullable=false) PaymentStatus status; @Column(nullable=false) Instant createdAt; protected PaymentEntity(){} PaymentEntity(UUID orderId,UUID userId){id=UUID.randomUUID();this.orderId=orderId;this.userId=userId;status=PaymentStatus.PENDING;createdAt=Instant.now();} }
