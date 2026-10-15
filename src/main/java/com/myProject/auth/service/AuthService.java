package com.myProject.auth.service;

import org.springframework.cache.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.myProject.auth.dto.*;
import com.myProject.security.jwt.JwtService;
import com.myProject.user.entity.*;
import com.myProject.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse signIn(SignInRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(
                "User not found with email: " + request.getEmail()));

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name());

        return new AuthResponse(token);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users", allEntries = true),
            @CacheEvict(value = "usersById", allEntries = true),
            @CacheEvict(value = "usersByEmail", allEntries = true)
    })
    public void signUp(SignupRequest request) {
        if (request.getRole() == Role.ROLE_ADMIN) {
                throw new IllegalArgumentException("Cannot sign-up as admin");
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .paymentMethod(request.getPaymentMethod())
                .role(request.getRole())
                .address(request.getAddress())
                .build();
        userRepository.save(user);
    }

}
