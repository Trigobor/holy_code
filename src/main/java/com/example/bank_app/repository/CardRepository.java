package com.example.bank_app.repository;

import com.example.bank_app.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> getCardByCardNumber(String cardNumber);
    List<Card> findByUserId(Long userId);
    Page<Card> findByUserId(Long userId, Pageable pageable);
    boolean existsByCardNumber(String cardNumber);
    void deleteCardByCardNumber(String cardNumber);

    Optional<Card> findByCardNumber(String cardNumber);

    @Modifying
    @Query("delete from Card c where c.user.username = :username")
    void deleteAllByUserUsername(@Param("username") String username);
}
