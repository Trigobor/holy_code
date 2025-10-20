package com.example.bank_app.DTO;

import java.math.BigDecimal;

public record TransferRequestDTO(String cardNumberFrom, String cardNumberTo, BigDecimal amount) { }
