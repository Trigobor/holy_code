package com.example.bank_app.controller;

import com.example.bank_app.DTO.CreateCardRequestDTO;
import com.example.bank_app.DTO.CreateCardResponseDTO;
import com.example.bank_app.DTO.DeleteCardRequestDTO;
import com.example.bank_app.DTO.GetCardResponseDTO;
import com.example.bank_app.mapper.CardMapper;
import com.example.bank_app.service.CardService;
import com.example.bank_app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    private final CardService cardService;

    @PostMapping("/cards")
    public CreateCardResponseDTO createCard(@RequestBody CreateCardRequestDTO createCardRequestDTO) {
        return CardMapper.fromEntityToCreateCardResponseDTO(cardService.createCard(createCardRequestDTO.userId()), true);
    }

    @GetMapping("/cards")
    public List<GetCardResponseDTO> getAllCards(@RequestParam(defaultValue = "true") boolean masked,
                                                @RequestParam(required = true) Integer size,
                                                @RequestParam(required = true) Integer page){
        List<GetCardResponseDTO> allCards = cardService.getAllCardsTotal().stream()
                .map(card -> CardMapper.fromEntityToGetCardResponseDTO(card, masked))
                .toList();

        int fromIndex = page * size;
        if (fromIndex >= allCards.size()) {
            return List.of();
        }

        int toIndex = Math.min(fromIndex + size, allCards.size());
        return allCards.subList(fromIndex, toIndex);
    }

    @GetMapping("/cards/{cardNumber}")
    public GetCardResponseDTO getCard(@PathVariable String cardNumber,
                                      @RequestParam(defaultValue = "true") boolean masked) {
        return CardMapper.fromEntityToGetCardResponseDTO(cardService.getCardByCardNumber(cardNumber), masked);
    }


    @DeleteMapping("/cards")
    public void deleteCard(@RequestBody DeleteCardRequestDTO deleteCardRequestDTO) {
        cardService.deleteCard(deleteCardRequestDTO.cardNumber());
    }


}
