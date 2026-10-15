package com.myProject.exception;

public class InsufficientResourceException extends RuntimeException {
    public InsufficientResourceException(String message) {
        super(message);
    }
}
