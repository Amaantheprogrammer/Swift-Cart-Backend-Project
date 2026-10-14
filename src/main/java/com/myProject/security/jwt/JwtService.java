package com.myProject.security.jwt;

import java.security.Key;
import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.expiration}")
    private Long jwtExpiration; // 1 hour

    public String generateToken(String email, String role) {
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return Jwts.builder()
            .setSubject(email)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // 1 hour expiration time
            .addClaims(claims)
            .signWith(getSignedKey(), SignatureAlgorithm.HS256)
            .compact();
    }

     // Method for signature verification + Extract all claims
    public Claims extractAllClaims(String token) { // Claim = User information
        return Jwts.parserBuilder()
                .setSigningKey(getSignedKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenValid(String token, String email) {
        return email.equals(extractEmail(token)) && !isTokenExpired(token);
    }

    // Private methods:
    // Method to get signed key
    private Key getSignedKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
}