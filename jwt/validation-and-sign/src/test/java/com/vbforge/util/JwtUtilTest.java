package com.vbforge.util;


import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for all three JwtUtil variants.
 *
 * No Spring context — beans are instantiated and initialized manually
 * using @PostConstruct-equivalent calls.
 */
class JwtUtilTest {

    private JwtUtil1 jwtUtil1;
    private JwtUtil2 jwtUtil2;
    private JwtUtil3 jwtUtil3;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil1 = new JwtUtil1();
        jwtUtil1.init();    // simulate @PostConstruct

        jwtUtil2 = new JwtUtil2();

        jwtUtil3 = new JwtUtil3();
        jwtUtil3.init();    // simulate @PostConstruct
    }

    // ----------------------------------------------------------------
    // JwtUtil1 tests
    // ----------------------------------------------------------------
    @Nested
    @DisplayName("JwtUtil1 — HS512, 256×'a'")
    class Util1Tests {

        @Test
        @DisplayName("Should generate a non-blank token")
        void generateToken_shouldReturnNonBlankToken() {
            String token = jwtUtil1.generateToken("alice", "pass", "USER");
            assertThat(token).isNotBlank();
        }

        @Test
        @DisplayName("Token should have 3 parts (header.payload.signature)")
        void generateToken_shouldHaveThreeParts() {
            String token = jwtUtil1.generateToken("alice", "pass", "USER");
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("extractUsername should return the subject")
        void extractUsername_shouldReturnSubject() {
            String token = jwtUtil1.generateToken("alice", "pass", "USER");
            assertThat(jwtUtil1.extractUsername(token)).isEqualTo("alice");
        }

        @Test
        @DisplayName("isTokenValid should return true for matching user")
        void isTokenValid_shouldReturnTrue() {
            String token = jwtUtil1.generateToken("bob", "pass", "ADMIN");
            assertThat(jwtUtil1.isTokenValid(token, "bob")).isTrue();
        }

        @Test
        @DisplayName("isTokenValid should return false for wrong username")
        void isTokenValid_shouldReturnFalseForWrongUser() {
            String token = jwtUtil1.generateToken("bob", "pass", "ADMIN");
            assertThat(jwtUtil1.isTokenValid(token, "alice")).isFalse();
        }
    }

    // ----------------------------------------------------------------
    // JwtUtil2 — weak key
    // ----------------------------------------------------------------
    @Nested
    @DisplayName("JwtUtil2 — HS256, weak key 'TEST'")
    class Util2Tests {

        @Test
        @DisplayName("generateToken should throw due to WeakKeyException")
        void generateToken_shouldThrowWeakKeyException() {
            // JJWT 0.12.x enforces minimum key size at signing time
            assertThatThrownBy(() -> jwtUtil2.generateToken("alice", "pass"))
                    .isInstanceOf(JwtException.class);
        }
    }

    // ----------------------------------------------------------------
    // JwtUtil3 — HS256, 256×'b'
    // ----------------------------------------------------------------
    @Nested
    @DisplayName("JwtUtil3 — HS256, 256×'b'")
    class Util3Tests {

        @Test
        @DisplayName("Should generate a valid token")
        void generateToken_shouldReturnValidToken() {
            String token = jwtUtil3.generateToken("charlie", "pass", "USER");
            assertThat(jwtUtil3.isTokenValid(token, "charlie")).isTrue();
        }
    }

    // ----------------------------------------------------------------
    // Cross-validation — the most important security test
    // ----------------------------------------------------------------
    @Nested
    @DisplayName("Cross-validation — different secrets must not be interchangeable")
    class CrossValidationTests {

        @Test
        @DisplayName("Token from Util1 should be REJECTED by Util3")
        void util1Token_shouldBeRejectedByUtil3() {
            String util1Token = jwtUtil1.generateToken("alice", "pass", "USER");
            // tryValidateFromOtherUtil catches JwtException and returns false
            assertThat(jwtUtil3.tryValidateFromOtherUtil(util1Token, "alice")).isFalse();
        }

        @Test
        @DisplayName("Token from Util3 should be REJECTED by Util1")
        void util3Token_shouldBeRejectedByUtil1() {
            String util3Token = jwtUtil3.generateToken("alice", "pass", "USER");
            // Direct call to Util1 should throw SignatureException
            assertThatThrownBy(() -> jwtUtil1.extractUsername(util3Token))
                    .isInstanceOf(JwtException.class);
        }
    }

    // ----------------------------------------------------------------
    // Tampered token
    // ----------------------------------------------------------------
    @Nested
    @DisplayName("Tampered token — payload modification must be detected")
    class TamperedTokenTests {

        @Test
        @DisplayName("Modifying the payload should cause signature verification to fail")
        void tamperedPayload_shouldBeRejected() {
            String token = jwtUtil1.generateToken("alice", "pass", "USER");
            String[] parts = token.split("\\.");
            String tampered = parts[0] + "." + parts[1] + "TAMPERED" + "." + parts[2];

            assertThatThrownBy(() -> jwtUtil1.extractUsername(tampered))
                    .isInstanceOf(JwtException.class);
        }

        @Test
        @DisplayName("Modifying the signature alone should also be rejected")
        void tamperedSignature_shouldBeRejected() {
            String token = jwtUtil1.generateToken("alice", "pass", "USER");
            String[] parts = token.split("\\.");
            String tampered = parts[0] + "." + parts[1] + ".invalidsignatureXXX";

            assertThatThrownBy(() -> jwtUtil1.extractUsername(tampered))
                    .isInstanceOf(JwtException.class);
        }
    }
}