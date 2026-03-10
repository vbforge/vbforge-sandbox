package com.vbforge.jwtbasics.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbforge.jwtbasics.dto.request.LoginRequest;
import com.vbforge.jwtbasics.dto.request.RegisterRequest;
import com.vbforge.jwtbasics.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the full JWT auth flow.
 *
 * @SpringBootTest — loads the full application context
 * @AutoConfigureMockMvc — provides MockMvc for HTTP simulation
 *
 * Note: Requires a running MySQL instance or use @DataJpaTest
 * with H2 for a lighter test setup. Annotate with @ActiveProfiles("test")
 * and add an application-test.yml with H2 config for CI environments.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;

    @BeforeEach
    void cleanDb() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Register → should return 201 with a JWT token")
    void shouldRegisterSuccessfully() throws Exception {
        RegisterRequest req = new RegisterRequest("alice", "password123", "alice@example.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    @DisplayName("Register with duplicate username → should return 409 Conflict")
    void shouldRejectDuplicateUsername() throws Exception {
        RegisterRequest req = new RegisterRequest("bob", "password123", "bob@example.com");

        // First registration succeeds
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // Second registration with same username → 409
        RegisterRequest duplicate = new RegisterRequest("bob", "other123", "other@example.com");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Login with correct credentials → should return 200 with JWT")
    void shouldLoginSuccessfully() throws Exception {
        // Register first
        RegisterRequest reg = new RegisterRequest("charlie", "pass1234", "charlie@example.com");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));

        // Then login
        LoginRequest login = new LoginRequest("charlie", "pass1234");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("Login with wrong password → should return 401")
    void shouldRejectBadCredentials() throws Exception {
        RegisterRequest reg = new RegisterRequest("dave", "correct", "dave@example.com");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));

        LoginRequest bad = new LoginRequest("dave", "wrongpassword");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Access protected endpoint without JWT → should return 403")
    void shouldBlockUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Full flow: register → login → access protected endpoint")
    void fullAuthFlow() throws Exception {
        // 1. Register
        RegisterRequest reg = new RegisterRequest("eve", "mypassword", "eve@example.com");
        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andReturn();

        // 2. Extract token
        String responseBody = registerResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).get("token").asText();

        // 3. Access protected endpoint with Bearer token
        mockMvc.perform(get("/api/hello")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("eve"));
    }
}