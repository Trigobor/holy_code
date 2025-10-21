package com.example.bank_app.service;

import com.example.bank_app.ENUM.CardStatus;
import com.example.bank_app.entity.Card;
import com.example.bank_app.entity.User;
import com.example.bank_app.exception.*;
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

    @Transactional
    public Card getCardByCardNumber(String cardNumber) {
        Card result = cardRepository.getCardByCardNumber(cardNumber)
                .orElseThrow(() -> new EntityNotFoundException("Card not found: " + cardNumber));
        validateCardIfExpired(result);
        return result;
    }

    @Transactional
    public Card getCardByCardNumber(String cardNumber, Long userId) {
        Card result = cardRepository.getCardByCardNumber(cardNumber)
                .orElseThrow(() -> new EntityNotFoundException("Card not found: " + cardNumber));
        validateCardIfExpired(result);

        if (!result.getUser().getId().equals(userId))
            throw new UserNotCardOwnerException("The user " + userId + " is not the owner of requested card");

        return result;
    }

    public Page<Card> getCardsByUserId(Long userId,  Pageable pageable) {
        return cardRepository.findByUserId(userId, pageable);
    }

    @Transactional
    public Card createCard(Long userId, String cardHolderName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Card card = new Card();

        card.setCardNumber(generateUniqueCardNumber());
        card.setCardHolderName(cardHolderName);
        card.setUser(user);
        card.setBalance(BigDecimal.ZERO);
        card.setExpirationDate(YearMonth.now().plusYears(5));
        card.setStatus(CardStatus.ACTIVE);
        return cardRepository.save(card);
    }

    //без брокеров не придумал как лучше сделать функционал по запросу блокировки карты
    @Transactional
    public Card requestToBlockCard(Long userId, String cardNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Card blockingCard = getCardByCardNumber(cardNumber);

        if (!blockingCard.getUser().getId().equals(user.getId()))
            throw new UserNotCardOwnerException("The user " + userId + " is not the owner of card " + blockingCard.getId());

        if(!blockingCard.getStatus().equals(CardStatus.ACTIVE))
            throw new InvalidCardStatusException("Card cannot be blocked with status: " + blockingCard.getStatus());

        blockingCard.setBlockRequest(true);
        return blockingCard;
    }

    public BigDecimal getBalance(String cardNumber, Long userId) {
        Card card = cardRepository.getCardByCardNumber(cardNumber)
                .orElseThrow(() -> new EntityNotFoundException("Card not found: " + cardNumber));
        if (!card.getUser().getId().equals(userId))
            throw new UserNotCardOwnerException("The user " + userId + " is not the owner of requested card");

        return card.getBalance();
    }

    @Transactional
    public Card blockCard(String cardNumber) {
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        if (validateCardIfExpired(card))
            throw new CardExpiredException(String.format(CARD_EXPIRED_MESSAGE, card.getCardNumber()));

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

    @Transactional
    public Card unblockCard(String cardNumber) {
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        if (validateCardIfExpired(card))
            throw new CardExpiredException(String.format(CARD_EXPIRED_MESSAGE, card.getCardNumber()));

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
    public void transfer(String fromCardNumber, String toCardNumber, BigDecimal amount, Long userId) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        Card from = cardRepository.getCardByCardNumber(fromCardNumber)
                .orElseThrow(() -> new EntityNotFoundException("Card not found: " + fromCardNumber));
        if (validateCardIfExpired(from))
            throw new CardExpiredException(String.format(CARD_EXPIRED_MESSAGE, fromCardNumber));

        if (from.getStatus().equals(CardStatus.BLOCKED))
            throw new CardBlockedException("Card " + fromCardNumber + " is blocked");

        Card to = cardRepository.getCardByCardNumber(toCardNumber)
                .orElseThrow(() -> new EntityNotFoundException("Card not found: " + toCardNumber));
        if (validateCardIfExpired(to))
            throw new CardExpiredException(String.format(CARD_EXPIRED_MESSAGE, toCardNumber));

        if (!from.getUser().getId().equals(userId))
            throw new UserNotCardOwnerException("The user " + userId + " is not the owner of card " + from.getId());

        if (!to.getUser().getId().equals(userId))
            throw new UserNotCardOwnerException("The user " + userId + " is not the owner of card " + to.getId());

        if (from.getBalance().compareTo(amount) < 0)
            throw new BalanceException("Not enough funds");

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        cardRepository.save(from);
        cardRepository.save(to);
    }

    @Transactional
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

    //ПОБОЧНЫЙ ЭФФЕКТ - обновление статуса карты в БД
    private boolean validateCardIfExpired(Card card) {
        YearMonth now = YearMonth.now();

        if (card.getExpirationDate().isBefore(now)) {
            card.setStatus(CardStatus.EXPIRED);
            cardRepository.saveAndFlush(card);
            return true;
        }
        return false;
    }
}