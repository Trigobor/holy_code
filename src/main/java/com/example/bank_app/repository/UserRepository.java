package com.example.bank_app.repository;

import com.example.bank_app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    void deleteByUsername(String username);

    boolean existsByUsername(String username);
}
