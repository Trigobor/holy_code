package com.example.bank_app.service;

import com.example.bank_app.ENUM.CardStatus;
import com.example.bank_app.entity.Card;
import com.example.bank_app.entity.User;
import com.example.bank_app.exception.UserNotFoundException;
import com.example.bank_app.exception.BalanceException;
import com.example.bank_app.repository.CardRepository;
import com.example.bank_app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CardService cardService;

    @Test
    void createCard_shouldCreateNewActiveCard() {
        User user = User.builder().id(1L).username("alex").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.existsByCardNumber(anyString())).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        Card card = cardService.createCard(1L, "ALEX");

        assertNotNull(card);
        assertEquals(CardStatus.ACTIVE, card.getStatus());
        assertEquals(BigDecimal.ZERO, card.getBalance());
        assertEquals("ALEX", card.getCardHolderName());
        assertEquals(YearMonth.now().plusYears(5), card.getExpirationDate());
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void createCard_whenUserMissing_shouldThrowUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> cardService.createCard(1L, "NAME"));
    }

    @Test
    void transfer_success_shouldMoveMoney() {
        User user = User.builder().id(1L).build();
        Card from = Card.builder().cardNumber("1111").balance(new BigDecimal("100")).user(user).status(CardStatus.ACTIVE).expirationDate(YearMonth.now().plusMonths(1)).build();
        Card to = Card.builder().cardNumber("2222").balance(new BigDecimal("10")).user(user).status(CardStatus.ACTIVE).expirationDate(YearMonth.now().plusMonths(1)).build();

        when(cardRepository.getCardByCardNumber("1111")).thenReturn(Optional.of(from));
        when(cardRepository.getCardByCardNumber("2222")).thenReturn(Optional.of(to));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        cardService.transfer("1111", "2222", new BigDecimal("25"), 1L);

        assertEquals(new BigDecimal("75"), from.getBalance());
        assertEquals(new BigDecimal("35"), to.getBalance());
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void transfer_insufficientFunds_shouldThrowBalanceException() {
        User user = User.builder().id(1L).build();
        Card from = Card.builder().cardNumber("1111").balance(new BigDecimal("10")).user(user).status(CardStatus.ACTIVE).expirationDate(YearMonth.now().plusMonths(1)).build();
        Card to = Card.builder().cardNumber("2222").balance(new BigDecimal("10")).user(user).status(CardStatus.ACTIVE).expirationDate(YearMonth.now().plusMonths(1)).build();

        when(cardRepository.getCardByCardNumber("1111")).thenReturn(Optional.of(from));
        when(cardRepository.getCardByCardNumber("2222")).thenReturn(Optional.of(to));

        assertThrows(BalanceException.class, () ->
                cardService.transfer("1111", "2222", new BigDecimal("100"), 1L)
        );

        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void requestToBlockCard_notOwner_shouldThrow() {
        User owner = User.builder().id(2L).build();
        User caller = User.builder().id(1L).build();
        Card card = Card.builder().cardNumber("3333").user(owner).status(CardStatus.ACTIVE).expirationDate(YearMonth.now().plusMonths(1)).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(caller));
        when(cardRepository.getCardByCardNumber("3333")).thenReturn(Optional.of(card));

        assertThrows(RuntimeException.class, () -> cardService.requestToBlockCard(1L, "3333"));
    }
}
