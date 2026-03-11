# Code Review — jwt-spring-security

## 1. Security Issues (High Priority)

### 1.1 Passwords stored in plain text
**File:** `UserRepository.java`, `UserService.java`

Passwords are saved and compared as raw strings. This is a critical vulnerability — if the database is ever exposed, all user credentials are compromised.

**Fix:** Hash passwords with `BCryptPasswordEncoder` before storing, and use `matches()` when verifying.

```java
// UserService.java
private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

public void saveUser(String username, String password) {
    userRepository.createUser(username, passwordEncoder.encode(password), "ROLE_USER");
}

public String loginUser(String login, String password) {
    String stored = userRepository.findPasswordByUsername(login);
    if (!passwordEncoder.matches(password, stored)) {
        throw new UsernameNotFoundException("Invalid credentials");
    }
    return jwtUtil.generateToken(login, "ROLE_USER");
}
```

---

### 1.2 JWT secret hardcoded in `application.yaml`
**File:** `application.yaml`

The JWT secret is a literal string committed to the codebase. Anyone with repo access can forge tokens.

**Fix:** Use an environment variable and exclude `application.yaml` from VCS, or use a secrets manager.

```yaml
jwt:
  secret: ${JWT_SECRET}   # inject from environment
```

---

### 1.3 No null-check on `Authorization` header
**File:** `ApiController.java`

If a request arrives without an `Authorization` header, `header.substring(7)` will throw a `NullPointerException`, returning a `500` instead of a proper `401`.

```java
if (header == null || !header.startsWith("Bearer ")) {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    return null;
}
```

---

### 1.4 Security filter chain allows everything
**File:** `SecurityConfig.java`

`requestMatchers("/**").permitAll()` means Spring Security never rejects any request — the manual JWT check inside `ApiController` is your only gate, which is fragile and easy to forget on new endpoints.

**Fix:** Move JWT validation to a proper `OncePerRequestFilter` and configure Spring Security to protect routes:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()
    .anyRequest().authenticated()
)
.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
```

---

## 2. Architecture / Design

### 2.1 JWT validation logic belongs in a filter, not a controller
**File:** `ApiController.java`

Manually extracting and validating the token inside a business controller couples security logic to application logic. Every protected endpoint would need to repeat this boilerplate.

**Fix:** Create a `JwtAuthenticationFilter extends OncePerRequestFilter` that populates `SecurityContextHolder` before the request reaches any controller. Controllers then just trust the security context.

---

### 2.2 `UserRepository` should use an ORM entity or at least close `PreparedStatement`
**File:** `UserRepository.java`

The `PreparedStatement` is not closed — only the `Connection` is closed by try-with-resources. While JDBC drivers usually close statements when the connection closes, it's better practice to wrap `ps` in its own try-with-resources block.

Also consider using Spring Data JPA with a `@Entity` `User` class — it would eliminate the raw SQL entirely, give you automatic connection management, and integrate cleanly with Spring Security's `UserDetailsService`.

---

### 2.3 `existUser` does a credential check in the repository layer
**File:** `UserRepository.java`

Querying by both `username` AND `password` (plain text) in SQL is the pattern that breaks once you hash passwords — you can no longer filter by password in the query. Instead, fetch the user by username only and compare passwords in the service layer.

```java
// Repository — fetch by username only
public Optional<String> findPasswordByUsername(String username) { ... }

// Service — compare in application code
passwordEncoder.matches(rawPassword, storedHash)
```

---

## 3. Minor / Code Quality

### 3.1 `@Value` fields in `JwtUtil` are not `final`
**File:** `JwtUtil.java`

`secret` and `expirationMs` are Spring-injected but mutable. Consider using constructor injection via `@ConfigurationProperties` for a cleaner, testable setup.

---

### 3.2 Unused imports / dead code in `JwtUtil`
**File:** `JwtUtil.java`

`isTokenValid(String token, UserDetails userDetails)` and `HashMap` are imported/defined but never called. Remove or use them to keep the class clean.

---

### 3.3 `SignupDTO` and `LoginDTO` are identical
**File:** `LoginDTO.java`, `SignupDTO.java`

Both DTOs have the exact same fields. You could use a single `AuthDTO`, or at minimum make `SignupDTO` extend `LoginDTO`. Only split them if they diverge in the future (e.g., signup adds an email field).

---

### 3.4 `signup` endpoint returns void with no status code
**File:** `AuthController.java`

A successful signup silently returns `200 OK` with an empty body. Consider returning `201 Created` and a confirmation payload, or at least a meaningful message.

```java
@PostMapping("/signup")
@ResponseStatus(HttpStatus.CREATED)
public Map<String, String> signup(@RequestBody SignupDTO signupDTO) {
    userService.saveUser(signupDTO.getLogin(), signupDTO.getPassword());
    return Map.of("message", "User created successfully");
}
```

---

### 3.5 No input validation on DTOs
There is no `@Valid` / `@NotBlank` on DTO fields. A request with an empty `login` or `password` will silently reach the database layer.

```java
// SignupDTO.java
@NotBlank
private String login;

@Size(min = 8)
private String password;
```

Then add `@Valid` to the controller parameter: `@RequestBody @Valid SignupDTO signupDTO`.
