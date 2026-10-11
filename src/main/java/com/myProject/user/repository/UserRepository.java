package com.myProject.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;

import com.myProject.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    // Create own method besides the in-built ones(findAll, findById, deleteAll, etc)
    // Refer for possible methods: https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html
    List<User> findByName(String name); // Works automatically by searching through the entity in camel case

    @EntityGraph(attributePaths = {"cart", "wishlist"})
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
}