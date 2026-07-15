package com.inridemart.cart;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
interface CartRepository extends JpaRepository<CartEntity,UUID>{Optional<CartEntity> findByUserId(UUID userId);}
