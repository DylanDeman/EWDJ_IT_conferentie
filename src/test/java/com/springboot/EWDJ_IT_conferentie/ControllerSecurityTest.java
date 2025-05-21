package com.springboot.EWDJ_IT_conferentie;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

@SpringBootTest
@AutoConfigureMockMvc
public class ControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest
    @CsvSource({
        "/rooms, true",
        "/rooms/1, true",
        "/api/rooms, true",
        "/api/events/2023-01-01, true"
    })
    @WithAnonymousUser
    public void testPublicEndpoints(String endpoint, boolean isPublic) throws Exception {
        mockMvc.perform(get(endpoint))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @MethodSource("securedEndpointTestCases")
    public void testSecuredEndpoints(String endpoint, String method, String userRole, ResultMatcher expectedStatus) throws Exception {
        if (userRole == null) {
            mockMvc.perform(method.equals("GET") ? get(endpoint) : post(endpoint))
                    .andExpect(expectedStatus);
        } else {
            mockMvc.perform(method.equals("GET") ? get(endpoint) : post(endpoint))
                    .andExpect(expectedStatus);
        }
    }
    
    private static Stream<Arguments> securedEndpointTestCases() {
        return Stream.of(
            // Admin endpoints with anonymous access
            Arguments.of("/admin/events", "GET", null, status().is3xxRedirection()),
            Arguments.of("/rooms/new", "GET", null, status().is3xxRedirection()),
            Arguments.of("/rooms/1/confirm", "GET", null, status().is3xxRedirection()),
            
            // Admin endpoints with USER role
            Arguments.of("/admin/events", "GET", "USER", status().isForbidden()),
            Arguments.of("/rooms/new", "GET", "USER", status().isForbidden()),
            Arguments.of("/rooms/1/confirm", "GET", "USER", status().isForbidden()),
            
            // Admin endpoints with ADMIN role
            Arguments.of("/admin/events", "GET", "ADMIN", status().isOk()),
            Arguments.of("/rooms/new", "GET", "ADMIN", status().isOk()),
            Arguments.of("/rooms/1/confirm", "GET", "ADMIN", status().isOk()),
            
            // User endpoints with anonymous access
            Arguments.of("/user/favorites", "GET", null, status().is3xxRedirection()),
            Arguments.of("/events/1/favorite", "POST", null, status().is3xxRedirection()),
            Arguments.of("/events/1/unfavorite", "POST", null, status().is3xxRedirection()),
            
            // User endpoints with USER role
            Arguments.of("/user/favorites", "GET", "USER", status().isOk()),
            Arguments.of("/events/1/favorite", "POST", "USER", status().is3xxRedirection()),
            Arguments.of("/events/1/unfavorite", "POST", "USER", status().is3xxRedirection())
        );
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testUserCanAccessUserFeatures() throws Exception {
        mockMvc.perform(get("/user/favorites"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAdminCanAccessAdminFeatures() throws Exception {
        mockMvc.perform(get("/admin/events"))
                .andExpect(status().isOk());
    }
}