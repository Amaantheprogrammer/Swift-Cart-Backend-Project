package com.myProject.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.myProject.category.entity.Category;

import java.util.*;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByNameContainingIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
