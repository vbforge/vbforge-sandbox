# jwt-spring-security-jpa Development Plan

## 1. Project Setup

Create a Spring Boot project (e.g., via https://start.spring.io) with:

### Dependencies
- Spring Web
- Spring Security
- Spring Data JPA
- MySQL Driver
- H2 Database (for testing)
- Lombok (optional, for boilerplate code)

### Additional Setup
- Add dependencies for JWT:
    - `jjwt` or `auth0-java-jwt`
- Configure `.env` for sensitive variables:
    - `DB_URL`
    - `DB_USERNAME`
    - `DB_PASSWORD`
    - `JWT_SECRET`

---

## 2. Database & Entities

### User Entity
**Fields:**
- `id`
- `username`
- `password`
- `email`
- `roles` (e.g., `Set<Role>`)

**Annotations:**
- `@Entity`
- `@Table(name = "users")`

---

### Role Entity (Recommended)
**Fields:**
- `id`
- `name` (e.g., `ADMIN`, `USER`)

**Annotations:**
- `@Entity`
- `@Table(name = "roles")`

---

### Relationships
- Many-to-Many between `User` and `Role`

---

### Profiles Configuration
- `application-dev.yml` → MySQL
- `application-test.yml` → H2

---

## 3. JWT Security Setup

### JWT Utility Class
**Methods:**
- `generateToken`
- `validateToken`
- `extractUsername`
- `extractRoles`

---

### Security Configuration

- Use:
    - `SecurityFilterChain` (Spring Boot 3+)
    - or `WebSecurityConfigurerAdapter` (legacy)

**Access Rules:**
- `/auth/**` → Public (login/signup)
- `/admin/**` → `ADMIN` only
- `/user/**` → `USER` or `ADMIN`

**Additional Setup:**
- Add JWT filter to validate tokens on each request

---

### Authentication Controller

**Endpoints:**
- `POST /auth/login`
- `POST /auth/signup`

**Behavior:**
- Return JWT on successful login

---

## 4. Initial Admin Setup

### Runner Setup
Use:
- `CommandLineRunner` or
- `ApplicationRunner`

### Logic
- Check if an `ADMIN` exists in DB
- If not:
    - Create default admin:
        - username: `admin`
        - password: `admin123`

### Notes
- Log credentials (only in development mode)

---

## 5. API Endpoints

### User Controller
- `GET /user/me` → Get current user info
- `PUT /user/me` → Update own profile

---

### Admin Controller
- `GET /admin/users` → List all users
- `POST /admin/users` → Create new user/admin
- `PUT /admin/users/{id}` → Update any user

---

### Authentication Controller
- `POST /auth/login`
- `POST /auth/signup`

---

## 6. Testing

### Unit Tests (JUnit 5 + Mockito)
- Test service layer:
    - `UserService`
    - `JwtService`

---

### Integration Tests
- Use `@SpringBootTest`
- Use `TestRestTemplate`
- Use H2 in-memory database

---

### Postman Collection
Create requests for:
- Login → `POST /auth/login`
- Admin endpoints (with JWT)
- User endpoints (with JWT)

---

## 7. Swagger API Documentation

### Setup
- Add SpringDoc OpenAPI dependency

### Annotations
- `@Tag`
- `@Operation`

### Access
- `/swagger-ui.html`

---

## 8. Validation & Verification

After each step:
- Run the application
- Test manually (Postman / curl)
- Verify database changes (MySQL / H2 console)
- Check logs for errors

---

## 9. Optional Enhancements (Future)

- Password reset endpoint
- Email verification
- Audit logs for admin actions