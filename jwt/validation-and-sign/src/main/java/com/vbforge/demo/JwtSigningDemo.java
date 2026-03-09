package com.vbforge.demo;

import com.vbforge.model.TokenResult;
import com.vbforge.util.JwtUtil1;
import com.vbforge.util.JwtUtil2;
import com.vbforge.util.JwtUtil3;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * JwtSigningDemo — runs on application startup and prints experiment results.
 *
 * Experiments performed:
 *  1. JwtUtil1 (HS512, 256×'a') — generate + validate same util ✅
 *  2. JwtUtil3 (HS256, 256×'b') — generate + validate same util ✅
 *  3. Cross-validation: JwtUtil1 token validated by JwtUtil3 — should FAIL ❌
 *  4. JwtUtil2 (HS256, weak 'TEST' key) — should throw WeakKeyException ❌
 *  5. Tampered token — manually modified payload — should fail signature check ❌
 *  6. Pre-recorded hardcoded token from original code — extract username
 */
@Component
public class JwtSigningDemo {

    private final JwtUtil1 jwtUtil1;
    private final JwtUtil2 jwtUtil2;
    private final JwtUtil3 jwtUtil3;

    public JwtSigningDemo(JwtUtil1 jwtUtil1, JwtUtil2 jwtUtil2, JwtUtil3 jwtUtil3) {
        this.jwtUtil1 = jwtUtil1;
        this.jwtUtil2 = jwtUtil2;
        this.jwtUtil3 = jwtUtil3;
    }

    public void run() {
        printHeader("JWT Signing & Validation Demo — vbforge/validation-and-sign");

        experiment1_Util1GenerateAndValidate();
        experiment2_Util3GenerateAndValidate();
        experiment3_CrossValidation();
        experiment4_WeakKey();
        experiment5_TamperedToken();
        experiment6_HardcodedToken();

        printFooter();
    }

    // ----------------------------------------------------------------
    // Experiment 1: JwtUtil1 — normal happy path
    // ----------------------------------------------------------------
    private void experiment1_Util1GenerateAndValidate() {
        printSection("Experiment 1 — JwtUtil1 (HS512, 256×'a'): generate + validate");

        String token = jwtUtil1.generateToken("TEST", "QQQQQQQQ", "USER");
        String extractedUsername = jwtUtil1.extractUsername(token);
        boolean valid = jwtUtil1.isTokenValid(token, "TEST");

        TokenResult result = new TokenResult(
                "JwtUtil1", jwtUtil1.getAlgorithmLabel(),
                "256 × 'a' (low entropy, sufficient length)",
                token, extractedUsername, valid
        );
        System.out.println(result);
    }

    // ----------------------------------------------------------------
    // Experiment 2: JwtUtil3 — same flow, different secret & algorithm
    // ----------------------------------------------------------------
    private void experiment2_Util3GenerateAndValidate() {
        printSection("Experiment 2 — JwtUtil3 (HS256, 256×'b'): generate + validate");

        String token = jwtUtil3.generateToken("ALICE", "PASS123", "ADMIN");
        String extractedUsername = jwtUtil3.extractUsername(token);
        boolean valid = jwtUtil3.isTokenValid(token, "ALICE");

        TokenResult result = new TokenResult(
                "JwtUtil3", jwtUtil3.getAlgorithmLabel(),
                "256 × 'b' (different secret from JwtUtil1)",
                token, extractedUsername, valid
        );
        System.out.println(result);
    }

    // ----------------------------------------------------------------
    // Experiment 3: Cross-validation — core security lesson
    // ----------------------------------------------------------------
    private void experiment3_CrossValidation() {
        printSection("Experiment 3 — Cross-validation: token from Util1, verified by Util3");
        System.out.println("""
                  Goal: prove that a token signed with secret-A cannot be validated
                  with secret-B, even if the algorithm is the same.
                """);

        String tokenFromUtil1 = jwtUtil1.generateToken("CROSS_TEST", "pass", "USER");

        // JwtUtil3 tries to verify a token signed by JwtUtil1 — must FAIL
        boolean crossValid = jwtUtil3.tryValidateFromOtherUtil(tokenFromUtil1, "CROSS_TEST");

        System.out.printf("  Token signed by Util1 (secret=256×'a')%n");
        System.out.printf("  Validated by Util3 (secret=256×'b') → %s%n%n",
                crossValid ? "✅ VALID (unexpected!)" : "❌ INVALID (correct — different secret)");
    }

    // ----------------------------------------------------------------
    // Experiment 4: Weak key — shows JJWT enforcement
    // ----------------------------------------------------------------
    private void experiment4_WeakKey() {
        printSection("Experiment 4 — JwtUtil2 (HS256, secret='TEST'): WeakKeyException");
        System.out.println("""
                  'TEST' = 4 bytes = 32 bits.
                  HS256 minimum = 256 bits (32 bytes).
                  JJWT 0.12.x enforces this and throws WeakKeyException.
                """);

        try {
            String token = jwtUtil2.generateToken("user", "pass");
            System.out.println("  Token generated (unexpected): " + token);
        } catch (Exception e) {
            System.out.printf("  ❌ Exception caught: %s%n  → %s%n%n",
                    e.getClass().getSimpleName(), e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // Experiment 5: Tampered token — signature mismatch
    // ----------------------------------------------------------------
    private void experiment5_TamperedToken() {
        printSection("Experiment 5 — Tampered token: payload modified after signing");
        System.out.println("""
                  An attacker intercepts the token and tries to change the payload
                  (e.g., change role from USER to ADMIN).
                  Even a single character change invalidates the signature.
                """);

        String original = jwtUtil1.generateToken("VICTIM", "pass", "USER");

        // A JWT is header.payload.signature — tamper with the payload (middle part)
        String[] parts = original.split("\\.");
        // Append a character to the payload — now the signature won't match
        String tampered = parts[0] + "." + parts[1] + "X" + "." + parts[2];

        try {
            jwtUtil1.extractUsername(tampered);
            System.out.println("  Token accepted (unexpected — security breach!)");
        } catch (JwtException e) {
            System.out.printf("  ❌ Tampered token rejected: %s%n  → Signature verification failed as expected.%n%n",
                    e.getClass().getSimpleName());
        }
    }

    // ----------------------------------------------------------------
    // Experiment 6: Hardcoded token from original code (preserved)
    // ----------------------------------------------------------------
    private void experiment6_HardcodedToken() {
        printSection("Experiment 6 — Hardcoded token from original project");
        System.out.println("""
                  This is the exact token from the original ValidationAndSignApplication.
                  It was signed with HS512 and secret=256×'a' (JwtUtil1).
                  It is expired (exp has passed), so extraction will throw ExpiredJwtException.
                """);

        // Original token from ValidationAndSignApplication.java
        String hardcoded = "eyJhbGciOiJIUzUxMiJ9.eyJwYXNzd29yZCI6IlFRUVFRUVFRUSIsInJvbGUiOiJVU0VSIiwic3ViIjoiVEVTVCIsImlhdCI6MTc3MTk1MzE3OCwiZXhwIjoxNzcxOTUzMjM4fQ.PD5eGlRKPS0krlP_eAHN_dVRU7GYPUMAw53mujnWZg4dePxg-BVfdsGJ-RyC_jlvqzNaSqKDuTFWFA4MnI7iYg";

        try {
            String username = jwtUtil1.extractUsername(hardcoded);
            System.out.println("  Extracted username: " + username);
        } catch (JwtException e) {
            System.out.printf("  ❌ %s: %s%n  → This is expected — the token expired long ago.%n%n",
                    e.getClass().getSimpleName(), e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // Print helpers
    // ----------------------------------------------------------------
    private void printHeader(String title) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("  " + title);
        System.out.println("=".repeat(70) + "\n");
    }

    private void printSection(String title) {
        System.out.println("──────────────────────────────────────────────────────────────────────");
        System.out.println("  " + title);
        System.out.println("──────────────────────────────────────────────────────────────────────");
    }

    private void printFooter() {
        System.out.println("=".repeat(70));
        System.out.println("  Done. Check REST endpoints at http://localhost:9192/test/*");
        System.out.println("  Paste any token at https://jwt.io to inspect its structure.");
        System.out.println("=".repeat(70) + "\n");
    }
}