package com.myProject.category.entity;

import java.util.*;

import com.myProject.product.entity.Product;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter 
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "categories")
@Entity
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    
    // Inverse side
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE}) 
    @Builder.Default
    private List<Product> products = new ArrayList<>();
}