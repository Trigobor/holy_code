package com.example.bank_app.DTO;

import java.math.BigDecimal;

public record GetCardBalanceResponseDTO(String cardId, BigDecimal balance) { }
