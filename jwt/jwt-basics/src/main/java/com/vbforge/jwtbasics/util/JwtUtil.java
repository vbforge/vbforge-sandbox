package com.vbforge.jwtbasics.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JwtUtil — the heart of this project.
 *
 * Responsibilities:
 *  1. Generate a JWT token for an authenticated user
 *  2. Extract claims (username, expiration, etc.) from a token
 *  3. Validate a token against a UserDetails object
 *
 * Algorithm: HMAC-SHA256 (HS256) — symmetric key signing.
 *   → Same secret key is used to sign AND verify.
 *   → Simple but key must be kept secret on the server.
 *   → Project 4 (OAuth2) will upgrade to RSA (asymmetric).
 *
 * Token structure (Base64 encoded, dot-separated):
 *   HEADER.PAYLOAD.SIGNATURE
 *
 *   Header  → { "alg": "HS256", "typ": "JWT" }
 *   Payload → { "sub": "username", "iat": ..., "exp": ... }
 *   Signature → HMAC_SHA256(base64(header) + "." + base64(payload), secret)
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMs;

    /**
     * Generates a JWT token for the given user.
     * The token subject (sub claim) is set to the username.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates a token with extra custom claims.
     * Useful for embedding roles, userId, etc. (explored in Project 3).
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())          // "sub" claim
                .issuedAt(new Date())                        // "iat" claim
                .expiration(new Date(System.currentTimeMillis() + expirationMs)) // "exp" claim
                .signWith(getSigningKey())                   // sign with HS256
                .compact();                                  // serialize to String
    }

    // ----------------------------------------------------------------
    // Token Validation
    // ----------------------------------------------------------------

    /**
     * Returns true if:
     *  1. The username in the token matches the UserDetails username
     *  2. The token is not expired
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ----------------------------------------------------------------
    // Claims Extraction
    // ----------------------------------------------------------------

    /** Extract the username (subject claim) from the token */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Extract the expiration date from the token */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic claim extractor — accepts a function that maps Claims → T.
     * This pattern lets callers extract any claim with one method.
     *
     * Example: extractClaim(token, claims -> claims.get("role", String.class))
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses and verifies the token signature.
     * Throws JwtException if the token is tampered, malformed, or expired.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())   // verify signature
                .build()
                .parseSignedClaims(token)      // parse & validate
                .getPayload();                 // get the claims body
    }

    // ----------------------------------------------------------------
    // Key Construction
    // ----------------------------------------------------------------

    /**
     * Builds the SecretKey from the configured secret string.
     *
     * Important: HS256 requires a key of at least 256 bits (32 bytes).
     * The secret in application.yml is a hex-encoded string — JJWT
     * handles the encoding internally via Keys.hmacShaKeyFor().
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }


}


















