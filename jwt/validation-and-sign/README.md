# validation-and-sign
Experiments with JWT (pure JWT mechanics), signing and validation using JJWT 0.12.x.

Three `JwtUtil` variants explore key length, algorithm choice, cross-secret rejection, and token tampering.

---

## Stack
- Java 21
- Spring Boot 3.5.0
- `io.jsonwebtoken:jjwt-api/impl/jackson:0.12.3`
- No database, no Spring Security — pure JWT mechanics

## Run
```bash
mvn spring-boot:run
# App starts on port 9192
# Experiments print to console on startup
```

---

## Project Structure

```
com.vbforge
├── ValidationAndSignApplication.java   ← entry point, CommandLineRunner
├── demo/
│   └── JwtSigningDemo.java             ← 6 startup experiments (console output)
├── util/
│   ├── JwtUtil1.java                   ← HS512, secret = 256×'a'
│   ├── JwtUtil2.java                   ← HS256, WEAK secret = "TEST" (intentional failure)
│   └── JwtUtil3.java                   ← HS256, secret = 256×'b'
├── controller/
│   └── ValidateTokenController.java    ← REST endpoints for manual HTTP experiments
└── model/
    └── TokenResult.java                ← display model for demo output
```

---

## Experiments (console output on startup)

| # | What it tests | Expected outcome |
|---|---|---|
| 1 | JwtUtil1 — generate + validate | ✅ Valid |
| 2 | JwtUtil3 — generate + validate | ✅ Valid |
| 3 | Util1 token validated by Util3 | ❌ Rejected (different secret) |
| 4 | Util2 — weak 'TEST' secret | ❌ WeakKeyException |
| 5 | Tampered token (payload modified) | ❌ SignatureException |
| 6 | Hardcoded token from original code | ❌ ExpiredJwtException |

---

## REST Endpoints

```bash
# Generate with Util1 (HS512)
GET  /test/util1/generate?username=alice&password=pass&role=USER

# Validate a Util1 token
POST /test/util1/validate
     Authorization: Bearer <token>

# Generate with Util2 (weak key — expect error)
GET  /test/util2/generate?username=alice&password=pass

# Generate with Util3 (HS256, different secret)
GET  /test/util3/generate?username=alice&password=pass&role=ADMIN

# Cross-validate: Util1 token verified by Util3's secret
POST /test/cross/validate?username=alice
     Authorization: Bearer <util1_token>
```

---

## Q&A — JWT Signing & Validation

### Q1: What is the difference between signing and encryption in JWT?

**Signing** (what this project does) proves the token has not been tampered with.  
The payload is only Base64-encoded — it is **readable by anyone** who intercepts the token.  
The signature is a cryptographic hash of `header.payload` using the secret key.  
If someone changes even one character of the payload, the signature no longer matches.

**Encryption** (JWE — JSON Web Encryption) actually hides the payload.  
The content is ciphertext and cannot be read without the decryption key.  
JJWT supports JWE but it is a separate topic — JWT in most web APIs means signed (JWS), not encrypted.

**Rule of thumb:** Sign when you need integrity. Encrypt when you need confidentiality.  
In auth flows: sign the token, never put sensitive data (passwords, SSN, etc.) in the payload.

---

### Q2: What does a JWT look like and what are the three parts?

```
eyJhbGciOiJIUzUxMiJ9  .  eyJzdWIiOiJhbGljZSJ9  .  PD5eGl...
       Header                    Payload              Signature
```

Each part is **Base64URL-encoded** and separated by a dot.

- **Header** — algorithm and token type: `{ "alg": "HS512", "typ": "JWT" }`
- **Payload** — claims: `{ "sub": "alice", "iat": 1715000000, "exp": 1715086400 }`
- **Signature** — `HMAC_SHA512(base64(header) + "." + base64(payload), secretKey)`

Paste any token at **https://jwt.io** to decode and inspect it visually.

---

### Q3: What are "claims" in a JWT?

Claims are key-value pairs stored in the payload. There are three types:

**Registered claims** (standard, well-known):
- `sub` — subject (usually the username)
- `iat` — issued at (Unix timestamp)
- `exp` — expiration (Unix timestamp)
- `iss` — issuer, `aud` — audience (used in OAuth2 / Project 4)

**Custom claims** — anything you add yourself, e.g. `"role": "ADMIN"`.  
In this project: `password` and `role` are custom claims.

⚠️ Do NOT put passwords in JWT claims — the payload is not encrypted.

---

### Q4: What is HMAC and how does HS256 / HS512 work?

**HMAC** (Hash-based Message Authentication Code) is a symmetric signing algorithm.  
The same secret key is used to **sign** and to **verify**.

```
Signature = HMAC_SHA256( base64(header) + "." + base64(payload), secretKey )
```

HS256 uses SHA-256 (produces a 256-bit hash).  
HS512 uses SHA-512 (produces a 512-bit hash — stronger, larger token).

**Symmetric** means both sides must share the same secret. If only one server handles auth, HS256/HS512 is fine. If you have separate auth and resource servers, use RSA (asymmetric) — covered in Project 4.

---

### Q5: Why does JJWT throw `WeakKeyException` for the secret `"TEST"`?

JJWT 0.12.x enforces minimum key sizes based on NIST recommendations:

| Algorithm | Minimum key size |
|---|---|
| HS256 | 256 bits (32 bytes) |
| HS384 | 384 bits (48 bytes) |
| HS512 | 512 bits (64 bytes) |

`"TEST"` = 4 ASCII characters = 4 bytes = 32 bits — far below the 256-bit minimum.  
This is enforced at **signing time** in 0.12.x (in older versions it was only a warning).

Fix: use at least 32 random bytes for HS256. Generate with:
```bash
openssl rand -hex 32
```

---

### Q6: Why does a Util1 token fail validation in Util3?

JwtUtil1 signs with secret `256×'a'`.  
JwtUtil3 verifies by recomputing `HMAC(header.payload, secret_256×'b')` and comparing to the signature.  
The recomputed value does not match the stored signature → `SignatureException`.

This is the **core security guarantee**: a token cannot be validated (or forged) without knowing the exact secret used to sign it. Even if the algorithm is the same, different secrets produce different signatures.

---

### Q7: What happens if someone tampers with the token payload?

The signature is computed over `base64(header).base64(payload)`.  
If you change anything in the payload — even one character — the recomputed signature will not match the stored one.  
JJWT throws `SignatureException` (a subtype of `JwtException`).

This is demonstrated in **Experiment 5** of this project.

---

### Q8: What is `@PostConstruct` and why is it used here?

`@PostConstruct` marks a method to run **once, after Spring has created and injected the bean**.  
It is used here to build the secret string (256 repetitions of a character) because:
- The secret is derived from logic, not a static value.
- `@Value` injection (from `application.yml`) has already happened by this point.
- The constructor runs before injection completes — so secret-building belongs in `@PostConstruct`.

Note: The original code had a bug — `secret = secret + "a"` on the first iteration where `secret` is `null` produces `"nulla"`. Fixed to use `StringBuilder`.

---

### Q9: What is the difference between `signWith(key)` in JJWT 0.9.x vs 0.12.x?

**Old API (0.9.x — deprecated):**
```java
.signWith(SignatureAlgorithm.HS256, "mySecret")  // string secret, algorithm explicit
Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody()
```

**New API (0.12.x — current):**
```java
.signWith(secretKey)        // SecretKey object; algorithm inferred from key length
Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
```

Key differences: the algorithm is now **inferred from key size** (no need to pass it explicitly), deprecated string-based signing was removed, and `parseClaimsJws` → `parseSignedClaims`.  
This project uses the 0.12.x API throughout.

---

### Q10: Is it safe to put a `role` claim in a JWT?

Roles are acceptable in JWT payloads because:
- They are not secret — the user's role is not sensitive information.
- They avoid a database lookup on every request (stateless).

However, keep in mind:
- The token is valid until it expires — role changes (e.g., user promoted to ADMIN) don't take effect until the old token expires.
- Use short expiration times if roles change frequently (see Project 2 — refresh tokens).
- Never put passwords, credit card numbers, or PII in JWT claims — the payload is Base64, not encrypted.

---

## Resources
- Theory (video): https://www.youtube.com/watch?v=UBUNrFtufWo&t=4s
- Token inspector: https://jwt.io
- JJWT docs: https://github.com/jwtk/jjwt

   
