# jwt-spring-security
How a JWT token travels through the application

---

## Goal of this project:

>A minimal Spring Boot app demonstrating how a JWT token travels through the application — from user signup and login, through token generation, to protected endpoint access.

---

## Stack
- Java 21
- Spring Boot 3.5.0
- `io.jsonwebtoken:jjwt-api/impl/jackson:0.12.5`
- MySQL 8
- Spring Security
- Lombok

---

## Run

```bash
# Start MySQL and make sure the DB is accessible, then:
mvn spring-boot:run
# App starts on port 8080
```

---

## How It Works

```
POST /api/auth/signup   →  save user to DB (plain password — see notes)
POST /api/auth/login    →  verify credentials → generate JWT → return token
GET  /data              →  extract token from header → validate → return data
```

The JWT carries two claims:
- `sub` — the username
- `role` — e.g. `ROLE_USER`

Token expiration is set to **24 hours** by default.

---

## REST Endpoints

### Health check
```bash
GET /api/auth/hello
# → "Hello World!"
```

### Sign up
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"login": "alice", "password": "secret"}'
```

### Log in — receive a token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"login": "alice", "password": "secret"}'
# → eyJhbGciOiJIUzI1NiJ9...
```

### Access protected data
```bash
curl http://localhost:8080/data \
  -H "Authorization: Bearer <token>"
# → {"key": "value"}
```

Requests without a valid `ROLE_USER` token receive `401 Unauthorized` or `403 Forbidden`.

---

## JWT Flow Diagram

```
Client          AuthController       UserService        JwtUtil
  |                   |                   |                |
  |-- POST /login --> |                   |                |
  |                   |-- existUser() --> |                |
  |                   |                   |-- generateToken()
  |                   |                   |                |
  |<-- JWT token ---- |                   |                |
  |                   |                   |                |
  |-- GET /data (Bearer token) ---------> ApiController    |
  |                                            |           |
  |                                            |-- extractUsername() / extractRole()
  |<-- {"key": "value"} ---------------------- |
```

---

## Configuration Reference

```yaml
jwt:
  secret: "..."        # HS256 signing key — min 256-bit
  expiration: 86400000 # Token TTL in milliseconds (86400000 = 24h)
```

---

##  Improvements

See `SUGGESTIONS.md`

---

## Resources
- Token inspector: https://jwt.io
- JJWT docs: https://github.com/jwtk/jjwt

   
