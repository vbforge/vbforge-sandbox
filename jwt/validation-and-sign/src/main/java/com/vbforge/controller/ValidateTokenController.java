package com.vbforge.controller;

import com.vbforge.util.JwtUtil1;
import com.vbforge.util.JwtUtil2;
import com.vbforge.util.JwtUtil3;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ValidateTokenController — REST endpoints to experiment with JWT via HTTP.
 *
 * All endpoints are open (no Spring Security in this project — it's a learning sandbox).
 *
 * Endpoints overview:
 *  GET  /test/util1/generate   → generate token with JwtUtil1 (HS512)
 *  POST /test/util1/validate   → validate + extract from JwtUtil1 token
 *  GET  /test/util2/generate   → generate token with JwtUtil2 (weak key — expect error)
 *  GET  /test/util3/generate   → generate token with JwtUtil3 (HS256, different secret)
 *  POST /test/cross/validate   → try to validate a Util1 token using Util3 (expect failure)
 *
 * How to test with curl — see README.md
 */
@RestController
@RequestMapping("/test")
public class ValidateTokenController {

    private final JwtUtil1 jwtUtil1;
    private final JwtUtil2 jwtUtil2;
    private final JwtUtil3 jwtUtil3;

    public ValidateTokenController(JwtUtil1 jwtUtil1, JwtUtil2 jwtUtil2, JwtUtil3 jwtUtil3) {
        this.jwtUtil1 = jwtUtil1;
        this.jwtUtil2 = jwtUtil2;
        this.jwtUtil3 = jwtUtil3;
    }

    // ----------------------------------------------------------------
    // JwtUtil1 — HS512
    // ----------------------------------------------------------------

    /**
     * Generate a token using JwtUtil1 (HS512, 256×'a').
     *
     * GET /test/util1/generate?username=alice&password=pass&role=USER
     */
    @GetMapping("/util1/generate")
    public ResponseEntity<Map<String, String>> generateUtil1(HttpServletRequest request) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String role     = request.getParameter("role");

        String token = jwtUtil1.generateToken(username, password, role);
        return ResponseEntity.ok(Map.of(
                "token",     token,
                "algorithm", jwtUtil1.getAlgorithmLabel(),
                "note",      "Paste this token at https://jwt.io to inspect the payload"
        ));
    }

    /**
     * Validate a JwtUtil1 token from the Authorization header.
     *
     * POST /test/util1/validate
     * Header: Authorization: Bearer <token>
     */
    @PostMapping("/util1/validate")
    public ResponseEntity<Map<String, Object>> validateUtil1(HttpServletRequest request) {
        String token = extractBearerToken(request);
        try {
            String username = jwtUtil1.extractUsername(token);
            boolean expired = jwtUtil1.isTokenExpired(token);
            return ResponseEntity.ok(Map.of(
                    "username", username,
                    "expired",  expired,
                    "valid",    !expired
            ));
        } catch (JwtException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error",   e.getClass().getSimpleName(),
                    "message", e.getMessage()
            ));
        }
    }

    // ----------------------------------------------------------------
    // JwtUtil2 — weak key (intentional failure demo)
    // ----------------------------------------------------------------

    /**
     * Attempt to generate with JwtUtil2 (weak 'TEST' key).
     * Expected result: WeakKeyException with explanation.
     *
     * GET /test/util2/generate?username=alice&password=pass
     */
    @GetMapping("/util2/generate")
    public ResponseEntity<Map<String, String>> generateUtil2(HttpServletRequest request) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        try {
            String token = jwtUtil2.generateToken(username, password);
            return ResponseEntity.ok(Map.of("token", token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error",       e.getClass().getSimpleName(),
                    "message",     e.getMessage(),
                    "explanation", "Secret '" + jwtUtil2.getSecret() + "' is " +
                            jwtUtil2.getSecret().length() + " bytes. " +
                            "HS256 requires ≥ 32 bytes."
            ));
        }
    }

    // ----------------------------------------------------------------
    // JwtUtil3 — HS256, different secret
    // ----------------------------------------------------------------

    /**
     * Generate a token using JwtUtil3 (HS256, 256×'b').
     *
     * GET /test/util3/generate?username=alice&password=pass&role=ADMIN
     */
    @GetMapping("/util3/generate")
    public ResponseEntity<Map<String, String>> generateUtil3(HttpServletRequest request) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String role     = request.getParameter("role");

        String token = jwtUtil3.generateToken(username, password, role);
        return ResponseEntity.ok(Map.of(
                "token",     token,
                "algorithm", jwtUtil3.getAlgorithmLabel()
        ));
    }

    // ----------------------------------------------------------------
    // Cross-validation experiment
    // ----------------------------------------------------------------

    /**
     * Validate a Util1-signed token using Util3's secret.
     * Expected: ❌ signature mismatch → false.
     *
     * POST /test/cross/validate
     * Header: Authorization: Bearer <util1_token>
     * Param:  username=<expected_username>
     */
    @PostMapping("/cross/validate")
    public ResponseEntity<Map<String, Object>> crossValidate(HttpServletRequest request) {
        String token    = extractBearerToken(request);
        String username = request.getParameter("username");

        boolean valid = jwtUtil3.tryValidateFromOtherUtil(token, username);
        return ResponseEntity.ok(Map.of(
                "valid",       valid,
                "explanation", valid
                        ? "Token accepted (unexpected!)"
                        : "Token rejected — signed with a different secret. This is correct behavior."
        ));
    }

    // ----------------------------------------------------------------
    // Helper
    // ----------------------------------------------------------------

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header. Expected: Bearer <token>");
        }
        return header.substring(7);
    }
}
