package com.myProject.order.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.myProject.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
    @Query("""
            SELECT DISTINCT o
            FROM Order o
            JOIN FETCH o.user
            JOIN FETCH o.orderItems oi
            JOIN FETCH oi.product p
            JOIN FETCH p.user
        """)
    Page<Order> findAllOrders(Pageable pageable);
    
    @Query("""
            SELECT DISTINCT o
            FROM Order o
            JOIN FETCH o.user
            JOIN FETCH o.orderItems oi
            JOIN FETCH oi.product p
            JOIN FETCH p.user
            WHERE o.id = :orderId
        """)
    Optional<Order> findOrderById(@Param("orderId") Long orderId);

    @Query("""
            SELECT DISTINCT o
            FROM Order o
            JOIN FETCH o.user
            JOIN FETCH o.orderItems oi
            JOIN FETCH oi.product p
            JOIN FETCH p.user
            WHERE o.user.id = :userId
           """)
    List<Order> findByUserId(@Param("userId") Long userId);

}
