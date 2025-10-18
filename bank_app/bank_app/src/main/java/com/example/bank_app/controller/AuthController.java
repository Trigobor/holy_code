package com.example.bank_app.controller;

import com.example.bank_app.DTO.AuthRequestDTO;
import com.example.bank_app.DTO.AuthResponseDTO;
import com.example.bank_app.security.JwtUtil;
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

    //private final UserRepository userRepository;
    //private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    /*
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (userRepository.findByUsername(req.username()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }
        User u = User.builder()
                .username(req.username())
                .password(passwordEncoder.encode(req.password()))
                .role(req.role() == null ? "USER" : req.role())
                .build();
        userRepository.save(u);
        return ResponseEntity.ok("User created");
    }
    */

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO req) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(req.username(), req.password());
        Authentication auth = authenticationManager.authenticate(token);
        UserDetails ud = (UserDetails) auth.getPrincipal();
        String jwt = jwtUtil.generateToken(ud);
        return ResponseEntity.ok(new AuthResponseDTO(jwt, "success"));
    }
}
