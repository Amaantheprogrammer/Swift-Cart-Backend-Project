package com.myProject.cart.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateCartRequest {

    @NotNull(message = "Product ID is a required field")
    private Long productId;
    
    @NotNull(message = "Quantity is a requied field")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}