package com.myProject.product.service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.myProject.category.entity.Category;
import com.myProject.security.user.CurrentUserUtil;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.myProject.category.repository.CategoryRepository;
import com.myProject.exception.*;
import com.myProject.product.dto.*;
import com.myProject.product.entity.*;
import com.myProject.product.repository.ProductRepository;
import com.myProject.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final CurrentUserUtil currentUserUtil;

    @Transactional(readOnly = true)
    @Cacheable(value = "products")
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAllProducts(pageable)
                .map(product -> modelMapper.map(product, ProductResponse.class));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "productsById", key = "#id")
    public ProductResponse getProductById(Long id) {
        Product product = getProductOrThrow(id);
        return modelMapper.map(product, ProductResponse.class);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "productsByNameContainingIgnoreCaseAndPriceLessThan", key = "#name + '_' + #price")
    public List<ProductResponse> getProductByNameContainingIgnoreCaseAndPriceLessThan(String name, BigDecimal price) {
        List<Product> products = (price != null)
                ? productRepository.findByNameContainingIgnoreCaseAndPriceLessThan(name, price)
                : productRepository.findByNameContainingIgnoreCase(name);
        return products.stream()
                .map(product -> modelMapper.map(product, ProductResponse.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "productsByCategoryIdAndPriceLessThan", key = "#id + '_' + #price")
    public List<ProductResponse> getProductsByCategoryIdAndPriceLessThan(Long id, BigDecimal price) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with ID: " + id);
        }
        List<Product> products = (price != null)
                ? productRepository.findByCategoryIdAndPriceLessThan(id, price)
                : productRepository.findProductsByCategoryId(id);
        return products.stream()
                .map(product -> modelMapper.map(product, ProductResponse.class))
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('SELLER')")
    @Transactional(readOnly = true)
    @Cacheable(value = "myProducts", key = "authentication.name")
    public List<ProductResponse> getMyProducts() {
        User user = currentUserUtil.getCurrentUser();
        List<Product> products = user.getProducts();
        return products.stream()
                .map(product -> modelMapper.map(product, ProductResponse.class))
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('SELLER')")
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "productsByNameContainingIgnoreCaseAndPriceLessThan", allEntries = true),
            @CacheEvict(value = "productsByCategoryIdAndPriceLessThan", allEntries = true)
    })
    public ProductResponse createNewProduct(NewProductRequest newProductRequest) {
        Category category = categoryRepository.findById(newProductRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + newProductRequest.getCategoryId()));
        Product product = modelMapper.map(newProductRequest, Product.class);
        product.setId(null);
        product.setUser(currentUserUtil.getCurrentUser());
        product.setCategory(category);
        ProductImage image = ProductImage.builder()
                .imageUrl(newProductRequest.getImageUrl())
                .product(product)
                .build();
        product.getProductImages().add(image);
        Product savedProduct = productRepository.save(product);
        return modelMapper.map(savedProduct, ProductResponse.class);
    }

    @PreAuthorize("hasRole('SELLER')")
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "productsById", key = "#id"),
            @CacheEvict(value = "productsByNameContainingIgnoreCaseAndPriceLessThan", allEntries = true),
            @CacheEvict(value = "productsByCategoryIdAndPriceLessThan", allEntries = true)
    })
    public ProductResponse updateProduct(Long id, UpdateProductRequest updateProductRequest) {
        Product product = getProductOrThrow(id);
        validateProductAuthority(product);
        if (updateProductRequest.getName() != null) {
            product.setName(updateProductRequest.getName());
        }
        if (updateProductRequest.getPrice() != null) {
            product.setPrice(updateProductRequest.getPrice());
        }
        if (updateProductRequest.getDescription() != null) {
            product.setDescription(updateProductRequest.getDescription());
        }
        if (updateProductRequest.getStockQuantity() != null) {
            product.setStockQuantity(updateProductRequest.getStockQuantity());
        }
        // No need to save in DB because Hibernate dirty checking handles it
        return modelMapper.map(product, ProductResponse.class);
    }

    @PreAuthorize("hasRole('SELLER')")
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "productsById", key = "#id"),
            @CacheEvict(value = "productsByNameContainingIgnoreCaseAndPriceLessThan", allEntries = true),
            @CacheEvict(value = "productsByCategoryIdAndPriceLessThan", allEntries = true),
            @CacheEvict(value = "myProducts", allEntries = true)
    })
    public void addImage(Long id, ImageRequest imageRequest) {
        Product product = getProductOrThrow(id);
        validateProductAuthority(product);
        boolean imageExists = product.getProductImages()
                .stream()
                .anyMatch(image -> image.getImageUrl().equals(imageRequest.getImageUrl()));
        if (imageExists) {
            throw new DuplicateResourceException("Image URL already exists");
        }
        ProductImage newProductImage = ProductImage.builder()
                .imageUrl(imageRequest.getImageUrl())
                .product(product)
                .build();
        product.getProductImages().add(newProductImage);
        productRepository.save(product);
    }

    @PreAuthorize("hasRole('SELLER')")
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "productsById", key = "#id"),
            @CacheEvict(value = "productsByNameContainingIgnoreCaseAndPriceLessThan", allEntries = true),
            @CacheEvict(value = "productsByCategoryIdAndPriceLessThan", allEntries = true),
            @CacheEvict(value = "myProducts", allEntries = true)
    })
    public void updateImage(Long id, Long imageId, ImageRequest imageRequest) {
        Product product = getProductOrThrow(id);
        validateProductAuthority(product);
        ProductImage productImage = product.getProductImages()
                .stream()
                .filter(image -> image.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with ID: " + imageId));
        productImage.setImageUrl(imageRequest.getImageUrl());
    }

    @PreAuthorize("hasRole('SELLER')")
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "productsById", key = "#id"),
            @CacheEvict(value = "productsByNameContainingIgnoreCaseAndPriceLessThan", allEntries = true),
            @CacheEvict(value = "productsByCategoryIdAndPriceLessThan", allEntries = true),
            @CacheEvict(value = "myProducts", allEntries = true)
    })
    public void deleteImage(Long id, Long imageId) {
        Product product = getProductOrThrow(id);
        validateProductAuthority(product);
        if (product.getProductImages().isEmpty()) {
            return;
        }
        boolean imageRemoved = product.getProductImages()
                .removeIf(image -> image.getId().equals(imageId));
        if (!imageRemoved) {
            throw new ResourceNotFoundException("Image not found with ID: " + imageId);
        }
        productRepository.save(product);
    }

    @PreAuthorize("hasRole('SELLER')")
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "productsById", key = "#id"),
            @CacheEvict(value = "productsByNameContainingIgnoreCaseAndPriceLessThan", allEntries = true),
            @CacheEvict(value = "productsByCategoryIdAndPriceLessThan", allEntries = true)
    })
    public void deleteProductById(Long id) {
        Product product = getProductOrThrow(id);
        validateProductAuthority(product);
        productRepository.deleteById(id);
    }

    // Private methods
    private void validateProductAuthority(Product product) {
        if (!product.getUser().getId().equals(currentUserUtil.getCurrentUser().getId())) {
            throw new AuthorizationDeniedException("You cannot update products of other sellers");
        }
    }

    private Product getProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
    }
}
