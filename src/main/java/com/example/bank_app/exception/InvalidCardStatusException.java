package com.example.bank_app.exception;

public class InvalidCardStatusException extends RuntimeException {
    public InvalidCardStatusException(String message) {
        super(message);
    }
}
