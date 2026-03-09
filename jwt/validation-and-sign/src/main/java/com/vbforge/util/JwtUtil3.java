package com.vbforge.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JwtUtil3 — Variant 3: HS256 with a different long secret (256 × 'b').
 *
 * Key experiment this class enables:
 *  A token signed by JwtUtil1 (secret = 256×'a') CANNOT be validated
 *  by JwtUtil3 (secret = 256×'b') — and vice versa.
 *
 * This demonstrates the core security guarantee of JWT signing:
 *  → The signature is bound to the exact secret used.
 *  → Different secret = different signature = validation failure.
 *  → This is why token forgery is computationally infeasible without the secret.
 *
 * Compare with JwtUtil1:
 *  - Same algorithm (HS256 here vs HS512 there — different signature sizes)
 *  - Different secret (256×'b' vs 256×'a')
 *  - Tokens are NOT interchangeable between the two
 */
@Component
public class JwtUtil3 {

    private String secret;

    private static final long EXPIRATION_MS = 1000 * 60;

    @PostConstruct
    public void init() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 256; i++) {
            sb.append("b");
        }
        secret = sb.toString();
    }

    // ----------------------------------------------------------------
    // Token Generation
    // ----------------------------------------------------------------

    public String generateToken(String username, String password, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("password", password);
        claims.put("role", role);
        return buildToken(username, claims);
    }

    private String buildToken(String username, Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(getSigningKey())
                .compact();
    }

    // ----------------------------------------------------------------
    // Token Parsing & Validation
    // ----------------------------------------------------------------

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenValid(String token, String expectedUsername) {
        return extractUsername(token).equals(expectedUsername) && !isTokenExpired(token);
    }

    /**
     * Attempts to validate a token that was signed by a DIFFERENT secret.
     * Returns false (instead of throwing) so demos can display the result cleanly.
     *
     * Internally: JJWT recomputes HMAC(token_header.token_body, this_secret)
     * and compares it against the signature in the token.
     * They won't match → SignatureException.
     */
    public boolean tryValidateFromOtherUtil(String token, String expectedUsername) {
        try {
            return isTokenValid(token, expectedUsername);
        } catch (JwtException e) {
            return false; // Expected: SignatureException — different secret
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String getAlgorithmLabel() { return "HS256 (key: 256×'b')"; }
}
