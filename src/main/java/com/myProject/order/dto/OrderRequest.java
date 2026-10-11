package com.myProject.order.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;
    @NotNull(message = "Quantity is required")
    private Integer quantity;
    // If blank, then user address is added by default
    private String address;
    // Can be blank for Orders with Cash On Delivery payment method
    private String accountNumber;
}