package com.inridemart.order;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
interface OrderJpaRepository extends JpaRepository<OrderEntity,UUID>{ Optional<OrderEntity> findByUserIdAndIdempotencyKey(UUID userId,String key); List<OrderEntity> findByUserIdOrderByCreatedAtDesc(UUID userId); }
