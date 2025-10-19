package com.example.bank_app.service;

import com.example.bank_app.ENUM.CardStatus;
import com.example.bank_app.entity.Card;
import com.example.bank_app.entity.User;
import com.example.bank_app.exception.CardActiveException;
import com.example.bank_app.exception.CardBlockedException;
import com.example.bank_app.exception.CardExpiredException;
import com.example.bank_app.exception.InvalidCardStatusException;
import com.example.bank_app.repository.CardRepository;
import com.example.bank_app.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CardService {
    private static final String CARD_EXPIRED_MESSAGE = "Card %s has expired";

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public Page<Card> getAllCardsTotal(Pageable pageable) {
        return cardRepository.findAll(pageable);
    }

    public Card getCardByCardNumber(String cardNumber) {
        return cardRepository.getCardByCardNumber(cardNumber)
                .orElseThrow(() -> new EntityNotFoundException("Card not found: " + cardNumber));
    }

    public List<Card> getCardsByUserId(Long userId) {
        return cardRepository.findByUserId(userId);
    }

    public Card createCard(Long userId, String cardHolderName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Card card = new Card();

        card.setCardNumber(generateUniqueCardNumber());
        card.setCardHolderName(cardHolderName);
        card.setUser(user);
        card.setBalance(BigDecimal.ZERO);
        card.setExpirationDate(YearMonth.now().plusYears(5));
        card.setStatus(CardStatus.ACTIVE);
        return cardRepository.save(card);
    }

    public Card blockCard(String cardNumber) {
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        validateCardNotExpired(card);

        switch (card.getStatus()) {
            case ACTIVE -> {
                card.setStatus(CardStatus.BLOCKED);
                return cardRepository.save(card);
            }
            case BLOCKED -> throw new CardBlockedException("Card already blocked");
            case EXPIRED -> throw new CardExpiredException("Card has expired");
            default -> throw new InvalidCardStatusException("Card cannot be blocked with status: " + card.getStatus());
        }
    }

    public Card unblockCard(String cardNumber) {
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        validateCardNotExpired(card);

        switch (card.getStatus()) {
            case BLOCKED -> {
                card.setStatus(CardStatus.ACTIVE);
                return cardRepository.save(card);
            }
            case ACTIVE -> throw new CardActiveException("Card already active");
            case EXPIRED -> throw new CardExpiredException("Card has expired");
            default -> throw new InvalidCardStatusException("Card cannot be activated with status: " + card.getStatus());
        }
    }

    @Transactional
    public void transfer(Long fromCardId, Long toCardId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        Card from = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new RuntimeException("Source card not found"));
        Card to = cardRepository.findById(toCardId)
                .orElseThrow(() -> new RuntimeException("Target card not found"));

        if (!(from.getStatus() == CardStatus.ACTIVE) || !(to.getStatus() == CardStatus.ACTIVE))
            throw new RuntimeException("One of the cards is blocked");

        if (from.getBalance().compareTo(amount) < 0)
            throw new RuntimeException("Not enough funds");

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        cardRepository.save(from);
        cardRepository.save(to);
    }

    public void deleteCard(String cardNumber) {
        cardRepository.deleteCardByCardNumber(cardNumber);
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

    private void validateCardNotExpired(Card card) {
        YearMonth now = YearMonth.now();

        if (card.getExpirationDate().isBefore(now)) {
            card.setStatus(CardStatus.EXPIRED);
            cardRepository.save(card);

            throw new CardExpiredException(String.format(CARD_EXPIRED_MESSAGE, card.getCardNumber()));
        }
    }
}