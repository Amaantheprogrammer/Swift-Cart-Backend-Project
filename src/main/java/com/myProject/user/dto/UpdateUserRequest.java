package com.myProject.user.dto;

import com.myProject.user.entity.PaymentMethod;
import jakarta.validation.constraints.Email;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
    private String name;
    @Email
    private String email;
    private PaymentMethod paymentMethod;
}
