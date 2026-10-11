package com.myProject.cart.entity;

import java.util.*;

import com.myProject.user.entity.User;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter 
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "carts")
@Builder
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> cartItems = new ArrayList<>();
}