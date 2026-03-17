package com.vbforge.jwtsecurity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hibernate.validator.internal.util.Contracts.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class JwtSpringSecurityApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void testSuccess() {
        String text = "text";
        assertEquals(4, text.length());
    }

}
