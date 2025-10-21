package com.example.bank_app.exception;

public class DuplicateUserNameException extends RuntimeException {
    public DuplicateUserNameException(String message) {
        super(message);
    }
}
