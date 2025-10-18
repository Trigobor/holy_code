package com.example.bank_app.service;

import com.example.bank_app.entity.Card;
import com.example.bank_app.entity.User;
import com.example.bank_app.repository.CardRepository;
import com.example.bank_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public List<Card> getAllCardsTotal() {
        return cardRepository.findAll();
    }

    public List<Card> getCardsByUserId(Long userId) {
        return cardRepository.findByUserId(userId);
    }

    public Card createCard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Card card = new Card();

        card.setCardNumber(generateUniqueCardNumber());
        card.setUser(user);
        card.setBalance(BigDecimal.ZERO);
        card.setActive(true);
        return cardRepository.save(card);
    }

    public Card blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        card.setActive(false);
        return cardRepository.save(card);
    }

    public Card unblockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        card.setActive(true);
        return cardRepository.save(card);
    }

    @Transactional
    public void transfer(Long fromCardId, Long toCardId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        Card from = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new RuntimeException("Source card not found"));
        Card to = cardRepository.findById(toCardId)
                .orElseThrow(() -> new RuntimeException("Target card not found"));

        if (!from.isActive() || !to.isActive())
            throw new RuntimeException("One of the cards is blocked");

        if (from.getBalance().compareTo(amount) < 0)
            throw new RuntimeException("Not enough funds");

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        cardRepository.save(from);
        cardRepository.save(to);
    }

    public void deleteCard(Long id) {
        cardRepository.deleteById(id);
    }

    private String generateCardNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    // плохое решение, если будет время - исправь генерацию
    // уникального номера карты без обращения к БД в цикле
    private String generateUniqueCardNumber() {
        String cardNumber;
        do {
            cardNumber = generateCardNumber();
        } while (cardRepository.existsByCardNumber(cardNumber));
        return cardNumber;
    }
}