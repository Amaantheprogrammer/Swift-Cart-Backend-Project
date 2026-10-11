package com.myProject.product.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.myProject.product.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"category", "productImages"})
    @Query("SELECT p FROM Product p")
    Page<Product> findAllProducts(Pageable pageable);
    
    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Product> findByNameContainingIgnoreCase(@Param("name") String name);
    
    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.price < :price")
    List<Product> findByNameContainingIgnoreCaseAndPriceLessThan(@Param("name")
        String name, @Param("price") BigDecimal price);
    
    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.category.id = :id")
    List<Product> findProductsByCategoryId(@Param("id") Long id);
    
    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.category.id = :id AND p.price < :price")
    List<Product> findByCategoryIdAndPriceLessThan(@Param("id") Long id, @Param("price") BigDecimal price);
    
}