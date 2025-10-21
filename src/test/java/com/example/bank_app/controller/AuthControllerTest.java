package com.example.bank_app.controller;

import com.example.bank_app.dto.AuthRequestDTO;
import com.example.bank_app.dto.AuthResponseDTO;
import com.example.bank_app.repository.UserRepository;
import com.example.bank_app.security.JwtUtil;
import com.example.bank_app.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @Test
    void login_returnsToken_whenValidCredentials() {
        AuthRequestDTO request = new AuthRequestDTO("testuser", "password");

        when(userService.existsByUsername("testuser")).thenReturn(true);

        Authentication auth = mock(Authentication.class);
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("testuser")
                .password("encodedpass")
                .roles("USER")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);

        when(jwtUtil.generateToken(userDetails)).thenReturn("fake-jwt-token");

        ResponseEntity<?> response = authController.login(request);

        assertEquals(200, response.getStatusCode().value());
        AuthResponseDTO body = (AuthResponseDTO) response.getBody();
        assertNotNull(body);
        assertEquals("fake-jwt-token", body.token());
        assertEquals("success", body.status());
    }
}
