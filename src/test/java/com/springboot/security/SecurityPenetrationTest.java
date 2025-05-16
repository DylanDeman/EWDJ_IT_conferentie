package com.springboot.security;

import com.springboot.domain.User;

import repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityPenetrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MyUser testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        testUser = new MyUser();
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");
        testUser.setRole(MyUser.UserRole.USER);
        testUser = userRepository.save(testUser);
    }

    @Test
    void testSqlInjection() throws Exception {
        mockMvc.perform(get("/events")
                .param("name", "'; DROP TABLE events; --"))
                .andExpect(status().isOk());
    }

    @Test
    void testXssAttack() throws Exception {
        String xssPayload = "<script>alert('XSS')</script>";
        
        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"" + xssPayload + "\", \"description\": \"Test\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCsrfProtection() throws Exception {
        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Test Event\", \"description\": \"Test\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testPathTraversal() throws Exception {
        mockMvc.perform(get("/events/../../../etc/passwd"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testBruteForceAttack() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/login")
                    .param("username", "testuser")
                    .param("password", "wrongpassword"))
                    .andExpect(status().is3xxRedirection());
        }
        
        mockMvc.perform(post("/login")
                .param("username", "testuser")
                .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void testSessionHijacking() throws Exception {
        mockMvc.perform(get("/user/profile")
                .header("X-Forwarded-For", "192.168.1.1")
                .header("User-Agent", "Mozilla/5.0"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testPrivilegeEscalation() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCommandInjection() throws Exception {
        mockMvc.perform(get("/events/search")
                .param("query", "test; rm -rf /"))
                .andExpect(status().isOk());
    }

    @Test
    void testOpenRedirect() throws Exception {
        mockMvc.perform(get("/redirect")
                .param("url", "https://malicious-site.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testInsecureDeserialization() throws Exception {
        String maliciousJson = "{\"@type\":\"java.lang.Runtime\",\"@type\":\"java.lang.ProcessBuilder\",\"command\":[\"calc.exe\"]}";
        
        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(maliciousJson))
                .andExpect(status().isBadRequest());
    }
} 