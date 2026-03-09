package com.vbforge.util;

import io.jsonwebtoken.Claims;
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
 * JwtUtil1 — Variant 1: HS512 with a long secret built at startup.
 *
 * What's different from JwtUtil2:
 *  - Uses HS512 (SHA-512 HMAC) instead of HS256 → larger signature (512 vs 256 bits)
 *  - Secret is 256 chars of 'a' — sufficient length but a bad real-world secret
 *    (low entropy; just for demonstration of key length requirements)
 *  - @PostConstruct initializes the secret after Spring injects the bean
 *
 * SIGNING vs ENCRYPTION reminder:
 *  This class SIGNS the token — the payload is Base64-encoded (visible to anyone),
 *  but the signature proves it hasn't been tampered with.
 *  It does NOT encrypt — the payload is NOT secret.
 *
 * API style: modern JJWT 0.12.x
 *  - Jwts.builder().claims(...).subject(...)   (fluent, no deprecated methods)
 *  - Jwts.parser().verifyWith(key).build()     (replaces old setSigningKey)
 */
@Component
public class JwtUtil1 {

    // Raw secret string — built in @PostConstruct
    private String secret;

    // Token validity: 60 seconds (demo purposes — very short)
    private static final long EXPIRATION_MS = 1000 * 60;

    /**
     * @PostConstruct runs once after Spring instantiates the bean.
     * Here we build the secret string (256 × 'a').
     *
     * Why 256 chars?
     *  HS512 requires a key of at least 512 bits (64 bytes).
     *  256 ASCII characters = 256 bytes — well above the minimum.
     *
     * In production: inject from @Value / environment variable, not hardcoded.
     */
    @PostConstruct
    public void init() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 256; i++) {
            sb.append("a");
        }
        // Fix from original: original code had `secret = secret + "a"` which
        // concatenated to "null" + "a" + "a" + ... on the first iteration
        // because `secret` was never initialized. Using StringBuilder avoids this.
        secret = sb.toString();
    }

    // ----------------------------------------------------------------
    // Token Generation
    // ----------------------------------------------------------------

    /**
     * Generates a signed JWT with username, password, and role in the payload.
     *
     * ⚠️  Including a password in a JWT payload is shown here for learning only.
     *      In real apps: NEVER put passwords in tokens — payload is not encrypted.
     */
    public String generateToken(String username, String password, String role) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("password", password);
        extraClaims.put("role", role);
        return buildToken(username, extraClaims);
    }

    private String buildToken(String username, Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)                     // custom claims (role, password)
                .subject(username)                  // "sub" standard claim
                .issuedAt(new Date())               // "iat"
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS)) // "exp"
                .signWith(getSigningKey())           // HS512 (key length determines algorithm)
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
        String username = extractUsername(token);
        return username.equals(expectedUsername) && !isTokenExpired(token);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())         // verify signature
                .build()
                .parseSignedClaims(token)            // parse + validate
                .getPayload();                       // return claims body
    }

    // ----------------------------------------------------------------
    // Key construction
    // ----------------------------------------------------------------

    /**
     * Converts the raw secret string into a javax.crypto.SecretKey.
     * Keys.hmacShaKeyFor() picks the right HMAC algorithm based on key length:
     *  ≥ 32 bytes  → HS256
     *  ≥ 48 bytes  → HS384
     *  ≥ 64 bytes  → HS512  ← our key (256 bytes) uses this
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String getAlgorithmLabel() { return "HS512 (key: 256×'a')"; }
}