package com.inridemart.payment;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
interface PaymentRepository extends JpaRepository<PaymentEntity,UUID>{ Optional<PaymentEntity> findByOrderIdAndUserId(UUID orderId,UUID userId); }
