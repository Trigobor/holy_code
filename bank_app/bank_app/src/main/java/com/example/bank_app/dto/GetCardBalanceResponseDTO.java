package com.example.bank_app.dto;

import java.math.BigDecimal;

public record GetCardBalanceResponseDTO(String cardId, BigDecimal balance) { }
