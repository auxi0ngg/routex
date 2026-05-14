package com.routex.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.routex.auth.dto.request.LoginRequest;
import com.routex.auth.dto.request.RegisterRequest;
import com.routex.auth.entity.Role;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.*;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("routex_auth_test")
        .withUsername("test")
        .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.3"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private static String accessToken;
    private static String refreshToken;

    @Test
    @Order(1)
    @DisplayName("Should register a new user successfully")
    void shouldRegisterUser() throws Exception {
        RegisterRequest request = new RegisterRequest(
            "test@routex.io", "TestPass@123", "Test", "User",
            "+911234567890", Role.CUSTOMER, null, "Chrome/MacOS", "127.0.0.1"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accessToken", not(emptyString())))
            .andExpect(jsonPath("$.refreshToken", not(emptyString())))
            .andExpect(jsonPath("$.email", is("test@routex.io")))
            .andExpect(jsonPath("$.role", is("CUSTOMER")));
    }

    @Test
    @Order(2)
    @DisplayName("Should fail registration with duplicate email")
    void shouldFailDuplicateEmail() throws Exception {
        RegisterRequest request = new RegisterRequest(
            "test@routex.io", "TestPass@123", "Test", "User",
            "+911234567890", Role.CUSTOMER, null, null, null
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code", is("EMAIL_ALREADY_EXISTS")));
    }

    @Test
    @Order(3)
    @DisplayName("Should login and receive JWT tokens")
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest request = new LoginRequest("test@routex.io", "TestPass@123", null, null);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken", not(emptyString())))
            .andExpect(jsonPath("$.refreshToken", not(emptyString())))
            .andExpect(jsonPath("$.tokenType", is("Bearer")))
            .andReturn();

        var responseNode = objectMapper.readTree(result.getResponse().getContentAsString());
        accessToken = responseNode.get("accessToken").asText();
        refreshToken = responseNode.get("refreshToken").asText();
    }

    @Test
    @Order(4)
    @DisplayName("Should fail login with wrong password")
    void shouldFailLoginWithWrongPassword() throws Exception {
        LoginRequest request = new LoginRequest("test@routex.io", "WrongPass@123", null, null);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code", is("INVALID_CREDENTIALS")));
    }

    @Test
    @Order(5)
    @DisplayName("Should validate a valid JWT token")
    void shouldValidateToken() throws Exception {
        Assumptions.assumeTrue(accessToken != null, "Access token must be set from login test");

        mockMvc.perform(get("/api/v1/auth/validate")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.valid", is(true)))
            .andExpect(jsonPath("$.email", is("test@routex.io")));
    }

    @Test
    @Order(6)
    @DisplayName("Should refresh tokens successfully")
    void shouldRefreshTokens() throws Exception {
        Assumptions.assumeTrue(refreshToken != null, "Refresh token must be set from login test");

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new com.routex.auth.dto.request.RefreshTokenRequest(refreshToken))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken", not(emptyString())))
            .andExpect(jsonPath("$.refreshToken", not(emptyString())));
    }

    @Test
    @Order(7)
    @DisplayName("Should logout and blacklist access token")
    void shouldLogout() throws Exception {
        Assumptions.assumeTrue(accessToken != null);

        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Bearer " + accessToken)
                .param("refreshToken", refreshToken))
            .andExpect(status().isNoContent());

        // Validate should now fail
        mockMvc.perform(get("/api/v1/auth/validate")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.valid", is(false)));
    }

    @Test
    @Order(8)
    @DisplayName("Should fail validation with invalid token format")
    void shouldFailInvalidTokenFormat() throws Exception {
        mockMvc.perform(get("/api/v1/auth/validate")
                .header("Authorization", "Bearer not.a.real.jwt.token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.valid", is(false)));
    }
}
