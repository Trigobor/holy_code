package com.example.bank_app.controller;

import com.example.bank_app.dto.AuthRequestDTO;
import com.example.bank_app.dto.AuthResponseDTO;
import com.example.bank_app.exception.UserNotCardOwnerException;
import com.example.bank_app.security.JwtUtil;
import com.example.bank_app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO req) {
        if (!userService.existsByUsername(req.username()))
            throw new UserNotCardOwnerException("Username not found");
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(req.username(), req.password());
        Authentication auth = authenticationManager.authenticate(token);
        UserDetails ud = (UserDetails) auth.getPrincipal();
        String jwt = jwtUtil.generateToken(ud);
        return ResponseEntity.ok(new AuthResponseDTO(jwt, "success"));
    }
}
