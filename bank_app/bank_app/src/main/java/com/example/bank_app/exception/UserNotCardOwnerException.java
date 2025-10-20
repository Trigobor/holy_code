package com.example.bank_app.exception;

public class UserNotCardOwnerException  extends RuntimeException {
    public UserNotCardOwnerException(String message) {
        super(message);
    }
}