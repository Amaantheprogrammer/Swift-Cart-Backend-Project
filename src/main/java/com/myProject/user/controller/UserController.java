package com.myProject.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.myProject.user.dto.*;
import com.myProject.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor // Objects with "final" keyword get added to the constructor
@RequestMapping("/users") // Reduces the effort of writing "/users" again and again
@Tag(name = "Users", description = "User APIs")
public class UserController {

    private final UserService userService;
    
    @GetMapping
    @Operation(
        summary = "Get user data",
        description = "Get all users if email is not passed, otherwise get specific user by email"
    )
    public ResponseEntity<?> getUsers(@RequestParam(required = false) String email) {
        if (email != null && !email.isBlank()) {
            return ResponseEntity.ok(userService.getUserByEmail(email));
        }
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get user by ID"
    )
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get user"
    )
    public ResponseEntity<UserResponse> getMyUser() {
        return ResponseEntity.ok(userService.getMyUser());
    }

    @PatchMapping
    @Operation(
        summary = "Update user",
        description = "Update user by UpdateUserRequest from request body"
    )
    public ResponseEntity<UserResponse> updateMyUser(@RequestBody UpdateUserRequest updateUserRequest) {
        return ResponseEntity.ok(userService.updateMyUser(updateUserRequest));
    }

    @DeleteMapping
    @Operation(
        summary = "Delete user"
    )
    public ResponseEntity<Void> deleteUser() {
        userService.deleteMyUser();
        return ResponseEntity.noContent().build();
    }
}
