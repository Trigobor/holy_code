package com.example.bank_app.mapper;

import com.example.bank_app.DTO.BlockUnblockCardResponseDTO;
import com.example.bank_app.DTO.CreateCardResponseDTO;
import com.example.bank_app.DTO.GetCardResponseDTO;
import com.example.bank_app.entity.Card;

public class CardMapper {
    public static CreateCardResponseDTO fromEntityToCreateCardResponseDTO(Card card, boolean masked) {
        if (masked) {
            return new CreateCardResponseDTO(maskCardNumber(card.getCardNumber()));
        }
        return new CreateCardResponseDTO(card.getCardNumber());
    }

    public static GetCardResponseDTO fromEntityToGetCardResponseDTO(Card card, boolean masked) {
        String number = masked ? maskCardNumber(card.getCardNumber()) : card.getCardNumber();
        return new GetCardResponseDTO(number, card.getBalance(), card.getExpirationDate(), card.getStatus(), card.getCardHolderName());
    }

    public static BlockUnblockCardResponseDTO fromEntityToBlockUnblockCardResponseDTO(Card card, String status) {
        return new BlockUnblockCardResponseDTO(maskCardNumber(card.getCardNumber()), status);
    }

    private static  String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) return cardNumber;
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
