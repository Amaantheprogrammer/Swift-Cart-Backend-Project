package com.myProject.seeder;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.myProject.user.entity.*;
import com.myProject.user.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor 
public class SeedAdmin {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct 
    public void seedAdmin() {
        if (!userRepository.existsByEmail("admin@ecommerce.com")) {
            User user = User.builder()
            .name("Amaan")
            .email("admin@ecommerce.com")
            .password(passwordEncoder.encode("admin123"))
            .role(Role.ROLE_ADMIN)
            .paymentMethod(PaymentMethod.CASH_ON_DELIVERY)
            .build();
            userRepository.save(user);
        }
    }
}
