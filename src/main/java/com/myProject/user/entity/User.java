package com.myProject.user.entity;

import java.util.*;

import com.myProject.cart.entity.Cart;
import com.myProject.order.entity.Order;
import com.myProject.product.entity.Product;

import com.myProject.wishlist.entity.Wishlist;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter 
@Table(name = "users")
@AllArgsConstructor

@NoArgsConstructor
@Builder // Enables creating readable way to create objects
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Cart cart;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Column(nullable = true)
    private String address;

    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();
    
    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Wishlist wishlist;
}