package com.vbforge.jwtbasics.util;

import com.vbforge.jwtbasics.entity.User;
import com.vbforge.jwtbasics.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.ExpiredJwtException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for JwtUtil.
 *
 * Tests JWT generation, claim extraction, and validation
 * without loading the full Spring context.
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        // Inject @Value fields manually (no Spring context in unit tests)
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437");
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 86400000L);

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("hashed_password")
                .email("test@example.com")
                .build();
    }

    @Test
    @DisplayName("Should generate a non-null token for a valid user")
    void shouldGenerateToken() {
        String token = jwtUtil.generateToken(testUser);
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("Generated token should contain the correct username as subject")
    void shouldExtractCorrectUsername() {
        String token = jwtUtil.generateToken(testUser);
        String username = jwtUtil.extractUsername(token);
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Generated token should contain the correct email as custom claim")
    void shouldExtractCorrectUserEmail() {
        String token = jwtUtil.generateToken(testUser);
        String userEmail = jwtUtil.extractEmail(token);
        assertThat(userEmail).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Token should be valid for the same user")
    void shouldValidateTokenForSameUser() {
        String token = jwtUtil.generateToken(testUser);
        assertThat(jwtUtil.isTokenValid(token, testUser)).isTrue();
    }

    @Test
    @DisplayName("Token should be invalid for a different user")
    void shouldRejectTokenForDifferentUser() {
        User otherUser = User.builder()
                .username("otheruser")
                .password("pass")
                .email("other@example.com")
                .build();

        String token = jwtUtil.generateToken(testUser);
        assertThat(jwtUtil.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    @DisplayName("Expired token should throw ExpiredJwtException")
    void shouldRejectExpiredToken() {
        // Set expiration to -1ms so the token is born already expired
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", -1L);
        String expiredToken = jwtUtil.generateToken(testUser);

        // JJWT throws ExpiredJwtException during parsing — before we can even
        // check the expiration ourselves. isTokenValid() is not the right call here;
        // the exception is the signal that the token is expired.
        assertThatThrownBy(() -> jwtUtil.isTokenValid(expiredToken, testUser))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("Token should have 3 parts separated by dots (header.payload.signature)")
    void shouldHaveThreePartsInToken() {
        String token = jwtUtil.generateToken(testUser);
        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);
    }
}