package com.example.bank_app.mapper;

import com.example.bank_app.DTO.CreateCardRequestDTO;
import com.example.bank_app.DTO.CreateCardResponseDTO;
import com.example.bank_app.entity.Card;

public class CardMapper {
    public static CreateCardResponseDTO fromEntityToCreateCardResponseDTO(Card card) {
        return  new CreateCardResponseDTO(maskCardNumber(card.getCardNumber()));
    }

    private static  String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) return cardNumber;
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
