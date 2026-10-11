package com.myProject.cart.service;

import java.util.Optional;

import com.myProject.security.user.CurrentUserUtil;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.myProject.cart.dto.CartResponse;
import com.myProject.cart.entity.*;
import com.myProject.cart.repository.CartRepository;
import com.myProject.exception.*;
import com.myProject.product.entity.Product;
import com.myProject.product.repository.ProductRepository;
import com.myProject.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final CurrentUserUtil currentUserUtil;

    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SELLER')")
    @Transactional(readOnly = true)
    public CartResponse getMyCart() {
        User user = currentUserUtil.getCurrentUser();
        Cart cart = user.getCart();
        if (cart == null) {
            cart = Cart.builder().user(user).build();
            cart = cartRepository.save(cart);
        }
        return modelMapper.map(cart, CartResponse.class);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'USER','SELLER')")
    @Transactional
    public CartResponse updateItemQuantity(Long productId, Integer quantity) {
        User user = currentUserUtil.getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
        Cart cart = user.getCart();
        if (cart == null) {
            cart = Cart.builder().user(user).build();
            user.setCart(cart);
        }
        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            if (newQuantity <= 0) {
                cart.getCartItems().remove(item);
            } else {
                if (product.getStockQuantity() < newQuantity) {
                    throw new BadRequestException("Insufficient stock for product: " + product.getName());
                }
                item.setQuantity(newQuantity);
            }
        } else {
            if (quantity <= 0) {
                throw new BadRequestException("Initial product quantity must be greater than zero.");
            }
            if (product.getStockQuantity() < quantity) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName());
            }
            CartItem newItem = CartItem.builder()
                    .product(product)
                    .cart(cart)
                    .quantity(quantity)
                    .build();
            cart.getCartItems().add(newItem);
        }
        return modelMapper.map(cartRepository.save(cart), CartResponse.class);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'USER','SELLER')")
    @Transactional
    public CartResponse removeProductFromCart(Long productId) {
        User user = currentUserUtil.getCurrentUser();
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with ID: " + productId);
        }     
        Cart cart = user.getCart();
        if (cart == null || cart.getCartItems().isEmpty()) {
            throw new ResourceNotFoundException("Cart is empty or does not exist for user ID: " + user.getId());
        }
        Optional<CartItem> itemToRemove = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();
        if (itemToRemove.isPresent()) {
            cart.getCartItems().remove(itemToRemove.get());
        } else {
            throw new ResourceNotFoundException("Product with ID: " + productId + " is not inside this cart");
        }
        return modelMapper.map(cartRepository.save(cart), CartResponse.class);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'USER','SELLER')")
    @Transactional
    public void clearCart() {
        User user = currentUserUtil.getCurrentUser();
        Cart cart = user.getCart();
        if (cart == null || cart.getCartItems().isEmpty()) return;
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }
}