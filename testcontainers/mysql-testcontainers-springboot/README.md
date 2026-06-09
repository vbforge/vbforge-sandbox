# mysql-testcontainers-springboot
REST API with Spring Boot, MySQL, Flyway, and Testcontainers integration testing

---

## **Features**
- **REST API**: Complete CRUD operations for Person entity
- **Testcontainers**: Automatic MySQL container management for integration tests
- **Flyway Migrations**: Version-controlled database schema
- **Validation**: Input validation with custom error responses
- **Global Exception Handling**: Consistent error format for all endpoints
- **Database Constraints**: Unique email and alias with proper error handling

---

## **Technologies**
- **Spring Boot 4.0.6**
- **Spring Data JPA** (Hibernate 7.2.x)
- **MySQL 8.0** (Production & Testing)
- **Flyway 10.x** (Database migrations)
- **Testcontainers 1.20.4** (Integration testing)
- **Maven** (Build tool)
- **JUnit 5 + AssertJ** (Testing)

---

## **Prerequisites**

- Java 21+
- Maven 3.9+
- Docker Desktop (for Testcontainers)

---

## **Quick Start**

### **Clone and Build**
```bash
git clone <repository-url>
cd mysql-testcontainers-springboot
mvn clean install -DskipTests
```

### **Run with Local MySQL**
```bash
# Start MySQL container
docker run -d --name mysql-local \
  -e MYSQL_ROOT_PASSWORD=55555555 \
  -e MYSQL_DATABASE=testcontainers_person_db \
  -p 3306:3306 mysql:8.0

# Run the application
mvn spring-boot:run
```

### **Run Tests with Testcontainers**
```bash
# Tests will automatically start MySQL in Docker
mvn test
```

---

## **API Endpoints**

| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|--------------|
| **POST** | `/api/persons` | Create new person | 201 Created, 400 Bad Request, 409 Conflict |
| **GET** | `/api/persons` | Get all persons | 200 OK |
| **GET** | `/api/persons/{id}` | Get person by ID | 200 OK, 404 Not Found |
| **GET** | `/api/persons/alias/{alias}` | Get person by alias | 200 OK, 404 Not Found |
| **GET** | `/api/persons/email/{email}` | Get person by email | 200 OK, 404 Not Found |
| **PUT** | `/api/persons/{id}` | Update person | 200 OK, 400 Bad Request, 404 Not Found, 409 Conflict |
| **DELETE** | `/api/persons/{id}` | Delete person | 204 No Content, 404 Not Found |
| **GET** | `/api/persons/count` | Get total person count | 200 OK |

---

## **Request/Response Examples**

### **Create Person**
```bash
POST /api/persons
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "alias": "johndoe",
  "phone": "+1234567890"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "alias": "johndoe",
  "phone": "+1234567890",
  "createdAt": "2026-06-10T10:30:00",
  "updatedAt": "2026-06-10T10:30:00"
}
```

### **Validation Error (400 Bad Request)**
```json
{
  "timestamp": "2026-06-10T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Input validation failed",
  "path": "/api/persons",
  "validationErrors": [
    {
      "field": "email",
      "message": "Invalid email format"
    },
    {
      "field": "alias",
      "message": "Alias must be between 3 and 50 characters"
    }
  ]
}
```

### **Duplicate Alias (409 Conflict)**
```json
{
  "timestamp": "2026-06-10T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Alias 'johndoe' already exists",
  "path": "/api/persons"
}
```

### **Not Found (404)**
```json
{
  "timestamp": "2026-06-10T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Person not found with id: 999",
  "path": "/api/persons/999"
}
```

---

## **Database Schema**

### **Person Table**
```sql
CREATE TABLE person (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    alias VARCHAR(50) NOT NULL UNIQUE,
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_email (email),
    INDEX idx_alias (alias)
);
```

### **Flyway Migrations**
- `V1__create_table_person.sql` - Initial schema
- `V2__insert_person_data.sql` - Sample data

---

## **Testing**

### **Test Structure**
```
src/test/java/
├── config/
│   └── AbstractDataBaseTest.java    # Testcontainers configuration
└── repository/
    └── PersonRepositoryTest.java     # Repository integration tests (17+ tests)

```

> in test/http folder find more files (IntelliJ HTTP client requests):
   - [health-check.http](src/test/http/health-check.http) 
   - [person-api.http](src/test/http/person-api.http) 
   - [person-api-test.http](src/test/http/person-api-test.http) 


### **Run Tests**
```bash
# Run all tests with Testcontainers
mvn test

# Run specific test class
mvn test -Dtest=PersonRepositoryTest

# Run with Docker container reuse (faster)
mvn test -Dtestcontainers.reuse.enable=true
```

### **Test Coverage**
- ✅ CRUD operations (Create, Read, Update, Delete)
- ✅ Custom queries (findByAlias, findByEmail)
- ✅ Unique constraints (alias & email)
- ✅ Nullable fields (phone)
- ✅ Auto-timestamp generation
- ✅ Edge cases and error scenarios

---

## **Project Structure**
```
src/
├── main/
│   ├── java/
│   │   └── com/vbforge/org/
│   │       ├── config/          # Configuration classes
│   │       ├── controller/       # REST endpoints
│   │       ├── dto/              # Request/Response DTOs
│   │       ├── entity/           # JPA entities
│   │       ├── exception/        # Custom exceptions
│   │       ├── mapper/           # DTO-Entity mapping
│   │       ├── repository/       # JPA repositories
│   │       └── service/          # Business logic
│   └── resources/
│       ├── db/migration/          # Flyway migrations
│       └── application.yml        # Configuration (see example in /helper_documentation)
└── test/
    ├── java/
    │   └── com/vbforge/org/
    │       ├── config/            # Testcontainers config
    │       └── repository/        # Repository tests
    └── resources/
        └── application-test.yml   # Test-specific config (optional)
```

- [/helper_documentation](helper_documentation)

---

## **Key Learnings & Takeaways**

### **Testcontainers Benefits**
- ✅ **No manual Docker setup** - Containers start automatically
- ✅ **Isolated tests** - Fresh database for each test run
- ✅ **Real MySQL** - Tests against actual database, not H2 mocks
- ✅ **Fast execution** - Container reuse between test runs
- ✅ **Clean shutdown** - Automatic container cleanup

### **Best Practices Implemented**
1. **DTOs separate from Entities** - Prevents over-fetching and exposure
2. **Global exception handling** - Consistent error responses
3. **Flyway for schema versioning** - No `ddl-auto: create-drop` in production
4. **Validation annotations** - Input validation at controller level
5. **Testcontainers abstract class** - Reusable configuration for all DB tests

---

## **Troubleshooting**

### **Docker Not Found**
```bash
# Start Docker Desktop
# Or run tests with H2 profile
mvn test -Dspring.profiles.active=test-h2
```

### **Port Already in Use**
```bash
# Stop existing MySQL container
docker stop mysql-local
docker rm mysql-local
```

### **Testcontainers Slow**
```bash
# Enable container reuse
echo "testcontainers.reuse.enable=true" > ~/.testcontainers.properties
```

---

## **Future Improvements**
- **Service Layer Tests** - Mock repository for unit tests
- **Controller Integration Tests** - `@WebMvcTest` with Testcontainers
- **Testcontainers with Docker Compose** - Multi-container scenarios
- **Performance Tests** - Load testing with Gatling
- **API Documentation** - OpenAPI/Swagger integration

---
