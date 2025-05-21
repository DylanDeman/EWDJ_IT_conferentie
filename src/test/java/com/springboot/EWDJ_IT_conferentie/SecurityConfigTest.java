package com.springboot.EWDJ_IT_conferentie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicEndpoints_ShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/events/1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());
    }

    @Test
    void adminEndpoints_ShouldRequireAdminRole() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        mockMvc.perform(get("/admin/events"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void userEndpoints_ShouldBeAccessibleWithUserRole() throws Exception {
        mockMvc.perform(get("/user/favorites"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/favorites"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminEndpoints_ShouldBeAccessibleWithAdminRole() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/events"))
                .andExpect(status().isOk());
    }

    @Test
    void login_ShouldRedirectToHomeOnSuccess() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "admin")
                .param("password", "admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void login_ShouldShowErrorOnFailure() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "wrong")
                .param("password", "wrong"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    @WithMockUser
    void logout_ShouldRedirectToHome() throws Exception {
        mockMvc.perform(post("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void csrf_ShouldBeEnabled() throws Exception {
        mockMvc.perform(post("/events")
                .param("name", "Test Event")
                .param("description", "Test Description"))
                .andExpect(status().isForbidden());
    }
} 