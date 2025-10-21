package com.example.bank_app.controller;

import com.example.bank_app.entity.Card;
import com.example.bank_app.ENUM.CardStatus;
import com.example.bank_app.service.CardService;
import com.example.bank_app.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private CardService cardService;

    @InjectMocks
    private AdminController adminController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).setControllerAdvice(new com.example.bank_app.exception.GlobalExceptionHandler()).build();
    }

    @Test
    void createCard_shouldReturnMaskedCardNumber() throws Exception {
        Card card = Card.builder().cardNumber("1111222233334444").balance(BigDecimal.ZERO).expirationDate(YearMonth.now().plusYears(5)).status(CardStatus.ACTIVE).cardHolderName("MOMENTUM CARD").build();
        when(cardService.createCard(1L, "MOMENTUM CARD")).thenReturn(card);

        String body = """
                {"userId":1}
                """;

        mockMvc.perform(post("/api/admin/cards")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").exists());

        verify(cardService, times(1)).createCard(1L, "MOMENTUM CARD");
    }
}
