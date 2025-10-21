package com.example.bank_app.dto;


import com.example.bank_app.ENUM.UserRole;

public record RegisterRequest(String username, String password, UserRole role) { }
