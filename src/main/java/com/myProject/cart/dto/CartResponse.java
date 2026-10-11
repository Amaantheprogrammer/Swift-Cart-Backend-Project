package com.myProject.cart.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartResponse {
    private Long id;
    private Long userId;
    private String userName;
    private List<CartItemResponse> cartItems;
    private BigDecimal total;
}