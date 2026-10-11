package com.myProject.user.service;

import java.util.List;
import java.util.stream.Collectors;

import com.myProject.security.user.CurrentUserUtil;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.myProject.exception.ResourceNotFoundException;
import com.myProject.user.dto.*;
import com.myProject.user.entity.User;
import com.myProject.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service // Service layer or Business logic
@RequiredArgsConstructor // Objects with "final" keyword get added to the constructor
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final CurrentUserUtil currentUserUtil;

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    @Cacheable(value = "users")
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> modelMapper.map(user, UserResponse.class))
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    @Cacheable(value = "usersById", key = "#id")
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return modelMapper.map(user, UserResponse.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    @Cacheable(value = "usersByEmail", key = "#email")
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return modelMapper.map(user, UserResponse.class);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SELLER')")
    @Transactional(readOnly = true)
    public UserResponse getMyUser() {
        return modelMapper.map(currentUserUtil.getCurrentUser(), UserResponse.class);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SELLER')")
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "users", allEntries = true),
        @CacheEvict(value = "usersById", allEntries = true),
        @CacheEvict(value = "usersByEmail", allEntries = true)
    })
    public UserResponse updateMyUser(UpdateUserRequest updateUserRequest) {
        User user = currentUserUtil.getCurrentUser();
        if (updateUserRequest.getName() != null) {
            user.setName(updateUserRequest.getName());
        }
        if (updateUserRequest.getEmail() != null) {
            user.setEmail(updateUserRequest.getEmail());
        }
        if (updateUserRequest.getPaymentMethod() != null && user.getPaymentMethod() != updateUserRequest.getPaymentMethod()) {
            user.setPaymentMethod(updateUserRequest.getPaymentMethod());
        }
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserResponse.class);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SELLER')")
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "users", allEntries = true),
        @CacheEvict(value = "usersById", allEntries = true),
        @CacheEvict(value = "usersByEmail", allEntries = true)
    })
    public void deleteMyUser() {
        Long currentUserId = currentUserUtil.getCurrentUser().getId();
        userRepository.deleteById(currentUserId);
    }

}
