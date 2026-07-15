package com.inridemart.cart;
import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(name="cart_items",uniqueConstraints=@UniqueConstraint(columnNames={"cart_id","product_id"})) class CartItemEntity { @Id UUID id; @ManyToOne @JoinColumn(name="cart_id") CartEntity cart; @Column(name="product_id") UUID productId; int quantity; protected CartItemEntity(){} CartItemEntity(CartEntity c,UUID p,int q){id=UUID.randomUUID();cart=c;productId=p;quantity=q;} }
