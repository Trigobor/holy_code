package com.example.bank_app.controller;

import com.example.bank_app.DTO.CreateCardRequestDTO;
import com.example.bank_app.DTO.CreateCardResponseDTO;
import com.example.bank_app.mapper.CardMapper;
import com.example.bank_app.service.CardService;
import com.example.bank_app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    private final CardService cardService;

    @PostMapping("/cards")
    public CreateCardResponseDTO createCard(@RequestBody CreateCardRequestDTO createCardRequestDTO) {
        return CardMapper.fromEntityToCreateCardResponseDTO(cardService.createCard(createCardRequestDTO.userId()));
    }
}
