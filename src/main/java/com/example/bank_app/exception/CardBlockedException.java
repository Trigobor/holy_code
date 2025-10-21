package com.example.bank_app.exception;

public class CardBlockedException extends RuntimeException {
    public CardBlockedException(String message) {
        super(message);
    }
}
