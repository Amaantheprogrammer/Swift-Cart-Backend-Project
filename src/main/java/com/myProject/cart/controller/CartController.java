package com.myProject.cart.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.myProject.cart.dto.*;
import com.myProject.cart.service.CartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/carts")
public class CartController {

    private final CartService cartService;

    @GetMapping("/my-cart")
    public ResponseEntity<CartResponse> getMyCart() {
        return ResponseEntity.ok(cartService.getMyCart());
    }
    
    @PostMapping("/items")
    public ResponseEntity<CartResponse> updateItemQuantity(@Valid @RequestBody UpdateCartRequest request) {
        CartResponse updatedCart = cartService.updateItemQuantity(request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeProductFromCart(@PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeProductFromCart(productId));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
}
