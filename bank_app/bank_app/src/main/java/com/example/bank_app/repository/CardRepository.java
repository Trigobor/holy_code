package com.example.bank_app.repository;

import com.example.bank_app.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByUserId(Long userId);
    boolean existsByCardNumber(String cardNumber);
}
