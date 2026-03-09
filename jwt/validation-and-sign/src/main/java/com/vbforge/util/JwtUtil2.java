package com.vbforge.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JwtUtil2 — Variant 2: HS256 with a DELIBERATELY WEAK short secret ("TEST").
 *
 * Purpose: demonstrate what happens when the secret is too short.
 *
 * "TEST" = 4 bytes = 32 bits.
 * HS256 requires a minimum of 256 bits (32 bytes).
 * → JJWT 0.12.x will throw WeakKeyException when you try to sign with this key.
 *
 * This class intentionally shows the failure so you can observe the error.
 * The try/catch in generateToken catches and reports it clearly.
 *
 * Real-world lesson:
 *  - Never use a short or guessable string as a JWT secret
 *  - Minimum: 32 random bytes for HS256, 64 for HS512
 *  - Best practice: generate with `openssl rand -hex 32`
 */
@Component
public class JwtUtil2 {

    /**
     * Intentionally weak — 4 bytes, far below the 32-byte minimum for HS256.
     * JJWT 0.12.x enforces minimum key lengths and will throw WeakKeyException.
     */
    private final String secret = "TEST";

    private static final long EXPIRATION_MS = 1000 * 60;

    // ----------------------------------------------------------------
    // Token Generation — will throw WeakKeyException with "TEST" secret
    // ----------------------------------------------------------------

    /**
     * Attempts to generate a token with a weak key.
     *
     * In JJWT 0.12.x: throws WeakKeyException immediately on .compact()
     * because "TEST" (4 bytes) is below the 32-byte minimum for HS256.
     *
     * This is intentional — run this and observe the exception in the console.
     */
    public String generateToken(String username, String password) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("password", password);
        return buildToken(username, claims);
    }

    private String buildToken(String username, Map<String, Object> claims) {
        // This will throw WeakKeyException — caught and rethrown with explanation
        try {
            return Jwts.builder()
                    .claims(claims)
                    .subject(username)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                    .signWith(getSigningKey())
                    .compact();
        } catch (WeakKeyException e) {
            // Re-throw with a clear explanation for learning purposes
            throw new WeakKeyException(
                    "SECRET TOO WEAK: '" + secret + "' is only " + secret.length() +
                            " bytes. HS256 requires ≥ 32 bytes. Original error: " + e.getMessage()
            );
        }
    }

    // ----------------------------------------------------------------
    // Parsing (same issue — weak key will also fail on verification)
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

    public String getAlgorithmLabel() { return "HS256 (WEAK key: 'TEST' — 4 bytes only)"; }
    public String getSecret()         { return secret; }
}
