package com.inridemart.auth.adapters.in.web;

import com.inridemart.auth.domain.ports.DomainEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DomainEventPublisher domainEventPublisher;

    @Test
    void registerLoginAndReadProfile() throws Exception {
        String registerResponse = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "api@example.com",
                                  "password": "StrongPass123",
                                  "role": "CUSTOMER"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = registerResponse.replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");
        String refreshToken = registerResponse.replaceAll(".*\"refreshToken\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("api@example.com"));

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "api@example.com",
                                  "password": "StrongPass123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken", notNullValue()));
    }
}
