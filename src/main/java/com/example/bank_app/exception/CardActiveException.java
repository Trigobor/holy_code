package com.example.bank_app.exception;

public class CardActiveException extends RuntimeException {
    public CardActiveException(String message) {
        super(message);
    }
}
