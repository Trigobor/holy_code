package com.example.bank_app.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    // Лучше хранить в application.yml и подменять на secret в prod
    @Value("${app.jwt.secret:change-me-to-secret-key}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms:3600000}") // 1 hour default
    private long jwtExpirationMs;

    private Key key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserDetails userDetails) {
        Map<String,Object> claims = new HashMap<>();
        // подставим роль(ы)
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        if (!authorities.isEmpty()) {
            claims.put("roles", authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        }
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtExpirationMs);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // логгирование по необходимости
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token)
                .getBody().getSubject();
    }
}
