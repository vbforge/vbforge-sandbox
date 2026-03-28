# jwt-spring-security-jpa
REST API with jwt, auth, roles, jpa User entity


---

## **Features**
- **User Authentication**: Signup, login, and JWT token generation.
- **Admin Panel**: Admins can manage users (CRUD operations).
- **Role-Based Access**: Users and admins have different permissions.
- **Flyway Migrations**: Database schema versioning.
- **Global Exception Handling**: Consistent error responses.

---

## **Technologies**
- **Spring Boot 3.5.0**
- **Spring Security 6**
- **JWT (JJWT 0.12.5)**
- **Flyway** (Database migrations)
- **MySQL/H2** (Database)
- **Lombok** (Reduce boilerplate)
- **JUnit 5 + Mockito** (Testing)

---

## **Prerequisites**

- Java 21
- Maven 3.9+
- MySQL (or H2 for testing)

---

## **API Endpoints**

### **Authentication**
| Endpoint          | Method | Description                     | Role   |
|-------------------|--------|---------------------------------|--------|
| `/api/auth/signup` | POST   | Register a new user            | Public |
| `/api/auth/login`  | POST   | Login and get JWT token         | Public |

### **Admin Panel**
| Endpoint                     | Method | Description                     | Role   |
|------------------------------|--------|---------------------------------|--------|
| `/api/admin/users`           | GET    | Get all users                  | ADMIN  |
| `/api/admin/users/{id}`      | GET    | Get user by ID                  | ADMIN  |
| `/api/admin/users/{id}`      | DELETE | Delete user by ID               | ADMIN  |
| `/api/admin/admins`          | GET    | Get all admins                  | ADMIN  |
| `/api/admin/admins`          | POST   | Create a new admin              | ADMIN  |
| `/api/admin/admins/{id}`     | GET    | Get admin by ID                 | ADMIN  |
| `/api/admin/admins/{id}`     | DELETE | Delete admin by ID              | ADMIN  |
| `/api/admin/me`              | PATCH  | Update own admin account        | ADMIN  |

### **User Panel**
| Endpoint          | Method | Description                     | Role   |
|-------------------|--------|---------------------------------|--------|
| `/api/user/me`    | PATCH  | Update own user account         | USER   |

---

## **Test Coverage**
- **Service Layer**: `AuthService`, `AdminService`
- **Controller Layer**: `AuthController`, `AdminController`
- **Exception Handling**: Global exception handler

---

## **Project Structure**
```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── vbforge/
│   │           └── jwtspringjpa/
│   │               ├── config/          # Configuration classes
│   │               ├── controller/      # REST controllers
│   │               ├── dto/             # Request/Response DTOs
│   │               ├── entity/          # JPA entities
│   │               ├── exception/       # Custom exceptions
│   │               ├── filter/           # JWT filter
│   │               ├── repository/      # JPA repositories
│   │               └── service/          # Business logic
│   └── resources/
│       ├── db/migration/    # Flyway migrations
│       └── application.yml  # Configuration
└── test/                    # Unit and integration tests
```

---

## **Security**
- **JWT Authentication**: Stateless authentication with JWT tokens.
- **Role-Based Access**: Admins and users have different permissions.
- **Password Encoding**: Uses `BCryptPasswordEncoder`.
- **CSRF Protection**: Disabled for stateless APIs.

---

## **Future Improvements**
- **Email Verification**: Add email verification for signup.
- **Refresh Tokens**: Implement refresh tokens for JWT.
- **Audit Logs**: Track admin actions.
- **Rate Limiting**: Protect against brute-force attacks.

---





