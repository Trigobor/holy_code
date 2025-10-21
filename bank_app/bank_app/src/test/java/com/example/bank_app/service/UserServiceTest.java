package com.example.bank_app.service;

import com.example.bank_app.ENUM.UserRole;
import com.example.bank_app.entity.User;
import com.example.bank_app.exception.UserNotFoundException;
import com.example.bank_app.repository.CardRepository;
import com.example.bank_app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_shouldEncodePasswordAndSave() {
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User u = userService.createUser("john", "pass", UserRole.USER);

        assertEquals("john", u.getUsername());
        assertEquals("encoded", u.getPassword());
        assertEquals(UserRole.USER, u.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_nonexistent_shouldThrow() {
        when(userRepository.findByUsername("nope")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.updateUser("nope", UserRole.ADMIN, "newpass"));
    }

    @Test
    void deleteUser_callsRepository() {
        doNothing().when(userRepository).deleteByUsername("john");
        userService.deleteUser("john");
        verify(userRepository, times(1)).deleteByUsername("john");
    }
}
