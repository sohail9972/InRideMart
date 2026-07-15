package com.inridemart.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/carts/me")
public class CartController {
    private final CartRepository carts;
    private final OrderClient orders;
    private final CatalogClient catalog;

    CartController(CartRepository carts, OrderClient orders, CatalogClient catalog) {
        this.carts = carts;
        this.orders = orders;
        this.catalog = catalog;
    }

    @GetMapping
    @Transactional(readOnly = true)
    Response get(Authentication authentication) {
        return Response.of(carts.findByUserId(userId(authentication)).orElseGet(() -> new CartEntity(userId(authentication))));
    }

    @PostMapping("/items")
    @Transactional
    Response add(Authentication authentication, @RequestBody Item item) {
        catalog.requireProduct(item.productId());
        CartEntity cart = carts.findByUserId(userId(authentication)).orElseGet(() -> new CartEntity(userId(authentication)));
        CartItemEntity current = cart.items.stream().filter(existing -> existing.productId.equals(item.productId())).findFirst().orElse(null);
        if (current == null) cart.items.add(new CartItemEntity(cart, item.productId(), item.quantity()));
        else current.quantity += item.quantity();
        return Response.of(carts.save(cart));
    }

    @PostMapping("/checkout")
    @Transactional
    Object checkout(Authentication authentication, @RequestHeader("Authorization") String bearerToken, @RequestHeader("Idempotency-Key") String idempotencyKey) {
        CartEntity cart = carts.findByUserId(userId(authentication)).orElseThrow(() -> new IllegalArgumentException("Cart is empty"));
        if (cart.items.isEmpty()) throw new IllegalArgumentException("Cart is empty");
        Object order = orders.create(cart.items.stream().map(item -> new Item(item.productId, item.quantity)).toList(), bearerToken, idempotencyKey);
        cart.items.clear();
        carts.save(cart);
        return order;
    }

    @PutMapping("/items/{productId}")
    @Transactional
    Response update(Authentication authentication, @PathVariable("productId") UUID productId, @RequestBody Item item) {
        CartEntity cart = carts.findByUserId(userId(authentication)).orElseThrow();
        CartItemEntity current = cart.items.stream().filter(existing -> existing.productId.equals(productId)).findFirst().orElseThrow();
        current.quantity = item.quantity();
        return Response.of(carts.save(cart));
    }

    @DeleteMapping("/items/{productId}")
    @Transactional
    Response remove(Authentication authentication, @PathVariable("productId") UUID productId) {
        CartEntity cart = carts.findByUserId(userId(authentication)).orElseThrow();
        cart.items.removeIf(item -> item.productId.equals(productId));
        return Response.of(carts.save(cart));
    }

    private UUID userId(Authentication authentication) { return (UUID) authentication.getPrincipal(); }

    record Item(@NotNull UUID productId, @Min(1) int quantity) { }
    record Response(UUID id, List<Item> items) {
        static Response of(CartEntity cart) { return new Response(cart.id, cart.items.stream().map(item -> new Item(item.productId, item.quantity)).toList()); }
    }
}
