package com.myProject.auth.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignInRequest {
    @NotBlank(message = "Email is a required field")
    private String email;
    @NotBlank(message = "Password is a required field")
    @Size(min = 6, message = "Password must have at least 6 characters")
    private String password;
}