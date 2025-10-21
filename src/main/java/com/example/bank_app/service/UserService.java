package com.example.bank_app.service;

import com.example.bank_app.ENUM.UserRole;
import com.example.bank_app.entity.User;
import com.example.bank_app.exception.DuplicateUserNameException;
import com.example.bank_app.exception.UserNotFoundException;
import com.example.bank_app.repository.CardRepository;
import com.example.bank_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CardRepository cardRepository;

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("There is no such user"));
    }

    @Transactional
    public User createUser(String username, String password, UserRole role) {
        if (userRepository.existsByUsername(username))
            throw new DuplicateUserNameException("Username already exists");
        User user = new User();
        user.setUsername(username);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(String username, UserRole role, String password) {
        return userRepository.findByUsername(username)
                .map(existing -> {
                    if (password != null && !password.isEmpty()) {
                        existing.setPassword(passwordEncoder.encode(password));
                    }
                    existing.setRole(role);
                    return userRepository.save(existing);
                })
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    // нужно бы разнести cardRepository.deleteAllByUserUsername(username); в сервис карт
    // и выполнять удаление юзера через фасад, но у меня мало времени, чтобы переписать
    @Transactional
    public void deleteUser(String username) {
        cardRepository.deleteAllByUserUsername(username);
        userRepository.deleteByUsername(username);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}