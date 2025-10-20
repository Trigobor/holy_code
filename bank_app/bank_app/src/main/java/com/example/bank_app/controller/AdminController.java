package com.example.bank_app.controller;

import com.example.bank_app.DTO.*;
import com.example.bank_app.entity.Card;
import com.example.bank_app.entity.User;
import com.example.bank_app.mapper.CardMapper;
import com.example.bank_app.mapper.UserMapper;
import com.example.bank_app.service.CardService;
import com.example.bank_app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.example.bank_app.mapper.CardMapper.fromEntityToBlockUnblockCardResponseDTO;
import static com.example.bank_app.mapper.UserMapper.fromEntityToUserDTO;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private static final String NO_CARD_HOLDER_NAME = "MOMENTUM CARD";

    private final UserService userService;
    private final CardService cardService;

    @PostMapping("/cards")
    public CreateCardResponseDTO createCard(@RequestBody CreateCardRequestDTO createCardRequestDTO,
                                            @RequestParam(defaultValue = NO_CARD_HOLDER_NAME) String cardHolderName) {
        return CardMapper.fromEntityToCreateCardResponseDTO(cardService.createCard(createCardRequestDTO.userId(), cardHolderName), true);
    }

    @GetMapping("/cards")
    public Page<GetCardResponseDTO> getAllCards(
            @RequestParam(defaultValue = "true") boolean masked,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cardsPage = cardService.getAllCardsTotal(pageable);

        return cardsPage.map(card -> CardMapper.fromEntityToGetCardResponseDTO(card, masked));
    }

    @PutMapping("/cards/{cardNumber}/block")
    public BlockUnblockCardResponseDTO blockUnblockCard(@PathVariable String cardNumber,
                                                        @RequestParam String action) {
        if(action.equals("block")) {
            return fromEntityToBlockUnblockCardResponseDTO(cardService.blockCard(cardNumber), "Blocked");
        } else {
            return fromEntityToBlockUnblockCardResponseDTO(cardService.unblockCard(cardNumber), "Activated");
        }
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

    @PostMapping("/users/create")
    public UserDTO createUser(@RequestBody UserDTO userDTO) {
        return fromEntityToUserDTO(userService.createUser(userDTO.username(),  userDTO.password(), userDTO.role()));
    }

    @GetMapping("/users")
    public Page<UserDTO> getUsers(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> usersPage = userService.getAllUsers(pageable);
        return usersPage.map(UserMapper::fromEntityToUserDTO);
    }

    @GetMapping("users/{username}")
    public UserDTO getUser(@PathVariable String username) {
        return fromEntityToUserDTO(userService.getUserByUsername(username));
    }

    @PutMapping("users/{username}/update")
    public UserDTO updateUser(@PathVariable String username,
                              @RequestBody UserDTO userDTO) {
        return fromEntityToUserDTO(userService.updateUser(username, userDTO.role(), userDTO.password()));
    }

    @DeleteMapping("users/{username}")
    public void deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
    }



}
