## Professional Improvements for AbstractDataBaseTest

### Version 1: Add Container Name and Basic Config

```java
package com.vbforge.org.config;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@DirtiesContext
public abstract class AbstractDataBaseTest {

    static final MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
            .withReuse(true)  // Reuse container between test runs (faster)
            .withLabel("test.type", "integration")  // Docker label for identification
            .withLabel("project", "vbforge-sandbox");

    @BeforeAll
    public static void setupContainers() {
        System.out.println("🐳 Starting MySQL Testcontainer...");
        mysql.start();
        System.out.println("✅ MySQL Testcontainer started at: " + mysql.getJdbcUrl());
        System.out.println("🔑 Container name: " + mysql.getContainerName());
    }

    @AfterAll
    public static void teardownContainers() {
        if (mysql.isRunning()) {
            System.out.println("🛑 Stopping MySQL Testcontainer...");
            mysql.stop();
            System.out.println("✅ MySQL Testcontainer stopped");
        }
    }

    @DynamicPropertySource
    public static void configureDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }
}
```

### Version 2: More Control with Custom Container Name

```java
package com.vbforge.org.config;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@DirtiesContext
public abstract class AbstractDataBaseTest {

    static final MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
            .withReuse(true)
            // Custom container name with timestamp
            .withCreateContainerCmdModifier(cmd -> cmd.withName(
                "test-mysql-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
            ))
            .withLabel("test.session", String.valueOf(System.currentTimeMillis()))
            .withLabel("test.class", System.getProperty("test.class", "unknown"));

    @BeforeAll
    public static void setupContainers() {
        System.out.println("🐳 Starting MySQL Testcontainer...");
        mysql.start();
        
        // Display useful info
        System.out.println("✅ MySQL Testcontainer Info:");
        System.out.println("   • Container name: " + mysql.getContainerName());
        System.out.println("   • JDBC URL: " + mysql.getJdbcUrl());
        System.out.println("   • Username: " + mysql.getUsername());
        System.out.println("   • Database: " + ((MySQLContainer<?>) mysql).getDatabaseName());
    }

    @AfterAll
    public static void teardownContainers() {
        if (mysql != null && mysql.isRunning()) {
            System.out.println("🛑 Cleaning up MySQL Testcontainer: " + mysql.getContainerName());
            mysql.stop();
            System.out.println("✅ MySQL Testcontainer stopped");
        }
    }

    @DynamicPropertySource
    public static void configureDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.baseline-on-migrate", () -> "true");
    }
}
```

### Version 3: Professional with Configuration Class (Most Professional)

```java
package com.vbforge.org.config;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;

@DirtiesContext
public abstract class AbstractDataBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractDataBaseTest.class);
    
    private static final String MYSQL_VERSION = "mysql:8.0";
    private static final String DATABASE_NAME = "testdb";
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpass";

    static final MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse(MYSQL_VERSION))
            .withDatabaseName(DATABASE_NAME)
            .withUsername(USERNAME)
            .withPassword(PASSWORD)
            .withReuse(true)  // Reuse container between test runs
            .withLabel("component", "test-database")
            .withLabel("application", "mysql-testcontainers-springboot")
            .withLogConsumer(new Slf4jLogConsumer(logger));  // Forward container logs to SLF4J

    @BeforeAll
    public static void setupContainers() {
        logger.info("🚀 Starting MySQL test container...");
        
        // Configure container reuse
        System.setProperty("testcontainers.reuse.enable", "true");
        
        mysql.start();
        
        logger.info("✅ MySQL test container started successfully");
        logger.info("📊 Connection details:");
        logger.info("   • Container ID: {}", mysql.getContainerId());
        logger.info("   • Container name: {}", mysql.getContainerName());
        logger.info("   • JDBC URL: {}", mysql.getJdbcUrl());
        logger.info("   • Host: {}", mysql.getHost());
        logger.info("   • Port: {}", mysql.getFirstMappedPort());
        logger.info("   • Database: {}", DATABASE_NAME);
        logger.info("   • Username: {}", USERNAME);
    }

    @AfterAll
    public static void teardownContainers() {
        if (mysql != null && mysql.isRunning()) {
            logger.info("🛑 Stopping MySQL test container...");
            mysql.stop();
            logger.info("✅ MySQL test container stopped");
        }
    }

    @DynamicPropertySource
    public static void configureDynamicProperties(DynamicPropertyRegistry registry) {
        logger.debug("🔧 Configuring Spring Boot dynamic properties for tests");
        
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        
        // Flyway configuration
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.baseline-on-migrate", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        
        // JPA configuration
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQL8Dialect");
        registry.add("spring.jpa.show-sql", () -> "true");
    }
    
    // Optional: Helper method to check container health
    public static boolean isContainerRunning() {
        return mysql != null && mysql.isRunning();
    }
    
    // Optional: Get container info for debugging
    public static String getContainerInfo() {
        return String.format("Container[%s] running at %s:%s",
                mysql.getContainerName(),
                mysql.getHost(),
                mysql.getFirstMappedPort());
    }
}
```

## Key Improvements Explained:

| Feature | Benefit |
|---------|---------|
| `.withDatabaseName()` | Explicit database name (not random) |
| `.withUsername()/withPassword()` | Explicit credentials for predictability |
| `.withReuse(true)` | ~50-70% faster test execution (reuses container) |
| `.withLabel()` | Easy to identify containers in `docker ps` |
| `.withLogConsumer()` | Container logs appear in your test output |
| `logger.info()` | Professional logging instead of `System.out` |
| `@DirtiesContext` | Ensures clean context per test class |
| Explicit configuration | No magic - clear what's being configured |

## What Sees in Docker:

```bash
docker ps
```

See containers with labels:
```
CONTAINER ID   IMAGE         NAMES                    CREATED
abc123...      mysql:8.0     /test-mysql-20240610-143022   Labels: test.type=integration
```

## What Sees in Logs:

```
INFO  AbstractDataBaseTest - 🚀 Starting MySQL test container...
INFO  AbstractDataBaseTest - ✅ MySQL test container started successfully
INFO  AbstractDataBaseTest - 📊 Connection details:
INFO  AbstractDataBaseTest -    • Container ID: abc123...
INFO  AbstractDataBaseTest -    • Container name: /test-mysql-20240610-143022
INFO  AbstractDataBaseTest -    • JDBC URL: jdbc:mysql://localhost:55000/testdb
INFO  AbstractDataBaseTest -    • Database: testdb
```

## Recommendation:

- ✅ Container reuse (speed)
- ✅ Labels (easy to identify)
- ✅ Better logging
- ✅ No over-engineering

This keeps it clean, maintainable, and professional! 🚀