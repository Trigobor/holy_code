package com.example.bank_app.controller;

import com.example.bank_app.dto.BlockUnblockCardResponseDTO;
import com.example.bank_app.dto.GetCardResponseDTO;
import com.example.bank_app.dto.TransferRequestDTO;
import com.example.bank_app.entity.Card;
import com.example.bank_app.mapper.CardMapper;
import com.example.bank_app.security.CustomUserDetails;
import com.example.bank_app.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import static com.example.bank_app.mapper.CardMapper.fromEntityToBlockUnblockCardResponseDTO;

@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
@RequiredArgsConstructor
public class UserController {
    private final CardService cardService;

    @PatchMapping("/cards/{cardNumber}/block")
    public ResponseEntity<BlockUnblockCardResponseDTO> requestBlock(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                    @PathVariable String cardNumber) {
        Card blocked = cardService.requestToBlockCard(userDetails.getId(), cardNumber);
        return ResponseEntity.ok(fromEntityToBlockUnblockCardResponseDTO(blocked, "Block requested"));
    }

    @GetMapping("/cards")
    public Page<GetCardResponseDTO> getAllCards(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cardsPage = cardService.getCardsByUserId(userDetails.getId(), pageable);

        return cardsPage.map(card -> CardMapper.fromEntityToGetCardResponseDTO(card, true));
    }

    @GetMapping("/cards/{cardNumber}")
    public GetCardResponseDTO getCard(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @PathVariable String cardNumber) {
        return CardMapper.fromEntityToGetCardResponseDTO(cardService.getCardByCardNumber(cardNumber, userDetails.getId()), true);
    }

    @GetMapping("/cards/{cardNumber}/balance")
    public ResponseEntity<BigDecimal> getCardBalance(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                     @PathVariable String cardNumber) {
        return ResponseEntity.ok(cardService.getBalance(cardNumber, userDetails.getId()));
    }

    @PostMapping("/cards/transfer")
    public ResponseEntity<String> transfer(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @RequestBody TransferRequestDTO transferRequestDTO){
        cardService.transfer(transferRequestDTO.cardNumberFrom(), transferRequestDTO.cardNumberTo(), transferRequestDTO.amount(), userDetails.getId());
        return ResponseEntity.ok("Transfer competed");
    }
}
