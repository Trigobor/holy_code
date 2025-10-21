package com.example.bank_app.dto;

import com.example.bank_app.ENUM.UserRole;

public record UserDTO(String username, String password, UserRole role) { }
