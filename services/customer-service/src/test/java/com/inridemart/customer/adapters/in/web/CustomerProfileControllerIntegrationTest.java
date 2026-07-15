package com.inridemart.customer.adapters.in.web;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerProfileControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void createReadAndUpdateCustomerProfile() throws Exception {
        String userId = "11111111-1111-1111-1111-111111111111";
        String authorization = authorizationFor(UUID.fromString(userId), "CUSTOMER");

        String createResponse = mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Aisha Mehta",
                                  "phoneNumber": "+919999999999",
                                  "address": {
                                    "addressLine1": "12 MG Road",
                                    "city": "Bengaluru",
                                    "state": "Karnataka",
                                    "postalCode": "560001",
                                    "country": "India"
                                  }
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.fullName").value("Aisha Mehta"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String profileId = createResponse.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/v1/customers/" + profileId)
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address.city").value("Bengaluru"));

        mockMvc.perform(get("/api/v1/customers/by-user/" + userId)
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Aisha Mehta"));

        mockMvc.perform(put("/api/v1/customers/" + profileId)
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Aisha Rao",
                                  "phoneNumber": "+918888888888",
                                  "address": {
                                    "addressLine1": "45 Brigade Road",
                                    "city": "Bengaluru",
                                    "state": "Karnataka",
                                    "postalCode": "560025",
                                    "country": "India"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Aisha Rao"))
                .andExpect(jsonPath("$.address.postalCode").value("560025"));
    }

    @Test
    void rejectsDuplicateProfileForUser() throws Exception {
        String authorization = authorizationFor(UUID.fromString("22222222-2222-2222-2222-222222222222"), "CUSTOMER");
        String request = """
                {
                  "fullName": "Rahul Nair"
                }
                """;

        mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsUnauthenticatedAndCrossUserAccess() throws Exception {
        UUID ownerId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        String ownerAuthorization = authorizationFor(ownerId, "CUSTOMER");

        String response = mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", ownerAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Priya Shah"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String profileId = response.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/v1/customers/" + profileId))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/customers/" + profileId)
                        .header("Authorization", authorizationFor(UUID.fromString("44444444-4444-4444-4444-444444444444"), "CUSTOMER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", authorizationFor(UUID.fromString("55555555-5555-5555-5555-555555555555"), "CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "66666666-6666-6666-6666-666666666666",
                                  "fullName": "Unauthorized User"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    private String authorizationFor(UUID userId, String role) {
        String token = Jwts.builder()
                .issuer("test-auth")
                .subject(userId.toString())
                .claim("role", role)
                .claim("token_use", "access")
                .signWith(Keys.hmacShaKeyFor("test-secret-test-secret-test-secret-32bytes".getBytes(StandardCharsets.UTF_8)))
                .compact();
        return "Bearer " + token;
    }
}
