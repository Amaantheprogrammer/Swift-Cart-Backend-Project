package com.myProject.order.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import com.myProject.user.entity.*;

import lombok.*;
import org.hibernate.mapping.Join;

@Getter
@Setter 
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders")
@Builder
@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime orderDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(nullable = false)
    private String address;

    public Order orElseThrow(Object object) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'orElseThrow'");
    }
}
// Set OrderStatus and PaymentStatus as PENDING by default

/*
Use of fetch type lazy:
If you have an admin dashboard displaying a list of 50 recent orders, 
a default EAGER fetch setup will trigger 50 extra database queries (the N+1 query problem) 
to fetch 50 separate user profiles. To avoid this. FetchType.LAZY is used
. 
*/