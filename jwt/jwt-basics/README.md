# jwt-basics
How JWT authentication works end-to-end (Spring Boot 3 + Spring Security 6)



---

## What This Project Covers

| Concept | Where to look |
|---|---|
| JWT structure (header.payload.signature) | `JwtUtil.java` — comments explain each part |
| HMAC-SHA256 signing & verification | `JwtUtil.getSigningKey()` + `generateToken()` |
| Extracting claims from a token | `JwtUtil.extractClaim()` — generic extractor pattern |
| OncePerRequestFilter implementation | `JwtAuthFilter.java` — full flow documented |
| Spring Security 6 filter chain | `SecurityConfig.java` |
| Stateless session configuration | `SecurityConfig` → `STATELESS` |
| CSRF disabled for APIs | `SecurityConfig` → `csrf.disable()` |
| DaoAuthenticationProvider wiring | `SecurityConfig.authenticationProvider()` |
| BCrypt password hashing | `SecurityConfig.passwordEncoder()` |
| @AuthenticationPrincipal usage | `ApiController.java` |
| Global exception handling | `GlobalExceptionHandler.java` |

---

## Endpoints

| Method | URL | Auth Required | Description |
|---|---|---|---|
| POST | `/api/auth/register` | ❌ No | Create a new account |
| POST | `/api/auth/login` | ❌ No | Login, receive JWT |
| GET | `/api/hello` | ✅ Bearer JWT | Simple protected endpoint |
| GET | `/api/me` | ✅ Bearer JWT | Returns current user info |

---

## How to Run

### Prerequisites
- Java 21
- MySQL 8 running on localhost:3306
- Maven 3.8+

### Setup

```bash
# 1. Create the database (auto-created if MySQL user has permissions)
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS jwt_basics_db;"

# 2. Update credentials in src/main/resources/application.yml
#    spring.datasource.username / password

# 3. Run
mvn spring-boot:run
```

---

## Testing the API (curl examples)

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password123","email":"alice@example.com"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password123"}'

# Access protected endpoint (replace TOKEN with the value from login response)
curl http://localhost:8080/api/hello \
  -H "Authorization: Bearer TOKEN"

# No token — should get 403
curl http://localhost:8080/api/hello
```

---

## JWT Token Anatomy

A JWT looks like this:
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGljZSIsImlhdCI6MTcxNTAwMDAwMCwiZXhwIjoxNzE1MDg2NDAwfQ.SIGNATURE
└──────────────────┘ └──────────────────────────────────────────────────────────────────────┘ └────────┘
      Header (alg)                          Payload (sub, iat, exp)                          Signature
```

Paste any token at **https://jwt.io** to inspect it visually.

### Claims used in this project
| Claim | Meaning | Set in |
|---|---|---|
| `sub` | Subject (username) | `JwtUtil.generateToken()` |
| `iat` | Issued At (Unix timestamp) | `JwtUtil.generateToken()` |
| `exp` | Expiration (Unix timestamp) | `JwtUtil.generateToken()` |

---

## Project Package Structure

```
com.vbforge.jwtbasics
├── JwtBasicsApplication.java
├── config/
│   └── SecurityConfig.java          ← Spring Security + filter chain
├── controller/
│   ├── AuthController.java          ← /api/auth/** (public)
│   └── ApiController.java           ← /api/** (protected)
├── dto/
│   ├── request/
│   │   ├── RegisterRequest.java
│   │   └── LoginRequest.java
│   └── response/
│       ├── AuthResponse.java
│       └── ErrorResponse.java
├── entity/
│   └── User.java                    ← Implements UserDetails
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── UserAlreadyExistsException.java
├── filter/
│   └── JwtAuthFilter.java           ← OncePerRequestFilter — JWT validation
├── repository/
│   └── UserRepository.java
├── service/
│   ├── AuthService.java             ← Register + Login logic
│   └── UserDetailsServiceImpl.java  ← Spring Security hook
└── util/
    └── JwtUtil.java                 ← Token generation, parsing, validation
```

---

## Key Learnings

1. **JWT is stateless** — the server doesn't store sessions. The token itself carries all needed info.
2. **Every request is independent** — `JwtAuthFilter` runs on every request to re-authenticate.
3. **BCrypt is one-way** — you can't reverse a hashed password. `AuthenticationManager` handles comparison.
4. **SecurityContext is per-request** — after `JwtAuthFilter` sets auth, any `@AuthenticationPrincipal` works.
5. **CSRF is off for APIs** — CSRF attacks rely on browsers sending cookies; JWTs in headers are immune.

---

