package com.myProject.product.controller;

import java.math.BigDecimal;
import java.util.List;

import com.myProject.product.dto.ImageRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.myProject.product.dto.*;
import com.myProject.product.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController // REST API usage
@RequiredArgsConstructor // Objects with "final" keyword get added to the constructor
@RequestMapping("/products") // Adds "products" to every http link
@Tag(name = "Products", description = "Product APIs")
public class ProductController {
    
    private final ProductService productService;

    @GetMapping
    @Operation(
        summary = "Get all products",
        description = "Get all products with paginated data"
    )
    public ResponseEntity<Page<ProductResponse>> getAllProducts(Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get all products",
        description = "Get all products with paginated data"
    )
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get the seller's own products"
    )
    public ResponseEntity<List<ProductResponse>> getMyProducts() {
        return ResponseEntity.ok(productService.getMyProducts());
    }

    @GetMapping("/search")
    @Operation(
        summary = "Get products with name and price",
        description = "Get products with request parameters name and price such that price isn't compulsory"
    )
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String name, @RequestParam(required = false) BigDecimal price) {
        /*
        GET /products/search?name=phone              → name search only
        GET /products/search?name=phone&price=500 → name + price filter
        */
        return ResponseEntity.ok(productService.getProductByNameContainingIgnoreCaseAndPriceLessThan(name, price));
    }
    
    @GetMapping("/category/{id}")
    @Operation(
        summary = "Get products by category ID"
    )
    public ResponseEntity<List<ProductResponse>> getProductsByCategoryId(@PathVariable Long id, @RequestParam(required = false) BigDecimal price) {
        return ResponseEntity.ok(productService.getProductsByCategoryIdAndPriceLessThan(id, price));
    }

    @PostMapping
    @Operation(
        summary = "Create new product",
        description = "Create new product by taking NewProductRequest object in the request body"
    )
    public ResponseEntity<ProductResponse> createNewProduct(@Valid @RequestBody NewProductRequest newProductRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createNewProduct(newProductRequest));
    }

    @PatchMapping("/{id}")
    @Operation(
        summary = "Update product",
        description = "Update product partially by ID and UpdateProductRequest object from the request body"
    )
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @RequestBody UpdateProductRequest updateProductRequest) {
        return ResponseEntity.ok(productService.updateProduct(id, updateProductRequest));
    }

    @PostMapping("/{id}/image")
    @Operation(
            summary = "Add image"
    )
    public ResponseEntity<Void> addImage(@PathVariable Long id, @Valid @RequestBody ImageRequest imageRequest) {
        productService.addImage(id, imageRequest);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/image/{imageId}")
    @Operation(
            summary = "Update image"
    )
    public ResponseEntity<Void> updateImage(@PathVariable Long id, @PathVariable Long imageId, @Valid @RequestBody ImageRequest imageRequest) {
        productService.updateImage(id, imageId, imageRequest);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/image/{imageId}")
    @Operation(
            summary = "Delete image"
    )
    public ResponseEntity<Void> deleteImage(@PathVariable Long id, @PathVariable Long imageId) {
        productService.deleteImage(id, imageId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete product by ID"
    )
    public ResponseEntity<Void> deleteProductById(@PathVariable Long id) {
        productService.deleteProductById(id);
        return ResponseEntity.noContent().build();
    }
}