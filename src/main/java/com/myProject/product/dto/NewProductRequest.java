package com.myProject.product.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewProductRequest {
    
    @NotBlank(message = "Name is a required field")
    private String name;

    @NotBlank(message = "ImageUrl is a required field")
    private String imageUrl;

    @NotNull(message = "Price is a required field")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    private BigDecimal price;

    private String description;

    @NotNull(message = "Stock is a required field")
    @Min(value = 0, message = "Stock quantity cannot be less than zero")
    private Integer stockQuantity;
    
    @NotNull(message = "Category ID is a required field")
    private Long categoryId;

}