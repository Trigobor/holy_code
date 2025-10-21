package com.example.bank_app.dto;

import java.math.BigDecimal;

public record TransferRequestDTO(String cardNumberFrom, String cardNumberTo, BigDecimal amount) { }
