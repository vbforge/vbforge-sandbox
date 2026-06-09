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

    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0")); //8.0

    @BeforeAll
    public static void setupContainers() {
        mysql.start();
    }

    @AfterAll
    public static void teardownContainers() {
        mysql.stop();
    }

    @DynamicPropertySource
    public static void configureDynamicProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);

    }

}
