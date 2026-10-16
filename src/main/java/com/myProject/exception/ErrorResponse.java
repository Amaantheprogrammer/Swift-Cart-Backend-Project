package com.myProject.exception;

import java.time.LocalDateTime;

import lombok.*;


@Builder
public class ErrorResponse {
    private String message;
    private LocalDateTime timestamp;
}