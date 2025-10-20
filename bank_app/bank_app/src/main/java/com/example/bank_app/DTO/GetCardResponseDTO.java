package com.example.bank_app.DTO;

import com.example.bank_app.ENUM.CardStatus;

import java.math.BigDecimal;
import java.time.YearMonth;

public record GetCardResponseDTO(String cardNumber, BigDecimal balance, YearMonth expirationDate, CardStatus status, String cardHolderName) { }
