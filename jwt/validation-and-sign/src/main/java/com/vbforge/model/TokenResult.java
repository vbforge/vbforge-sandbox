package com.vbforge.model;

/**
 * TokenResult — carries a generated JWT and its metadata for demo output.
 *
 * Used by JwtSigningDemo to print structured experiment results.
 */
public record TokenResult(
        String label,        // human-readable label, e.g. "JwtUtil1 (HS512)"
        String algorithm,    // signing algorithm used
        String secretInfo,   // description of the secret (never the actual value)
        String token,        // the generated JWT string
        String username,     // extracted subject for verification
        boolean valid        // was the token validated successfully?
) {
    @Override
    public String toString() {
        return """
                ┌─ %s ─────────────────────────────────────────
                │  Algorithm  : %s
                │  Secret     : %s
                │  Token      : %s
                │  Username   : %s
                │  Valid      : %s
                └──────────────────────────────────────────────────
                """.formatted(label, algorithm, secretInfo,
                token.substring(0, Math.min(60, token.length())) + "...",
                username, valid ? "✅ YES" : "❌ NO");
    }
}