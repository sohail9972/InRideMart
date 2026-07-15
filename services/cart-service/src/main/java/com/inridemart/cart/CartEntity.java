package com.inridemart.cart;
import jakarta.persistence.*; import java.util.*;
@Entity @Table(name="shopping_carts") class CartEntity { @Id UUID id; @Column(unique=true,nullable=false) UUID userId; @OneToMany(mappedBy="cart",cascade=CascadeType.ALL,orphanRemoval=true) List<CartItemEntity> items=new ArrayList<>(); protected CartEntity(){} CartEntity(UUID userId){id=UUID.randomUUID();this.userId=userId;} }
