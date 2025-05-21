package com.springboot.EWDJ_IT_conferentie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import service.UserService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testListFavorites_WithUserRole() throws Exception {
        // Arrange
        when(userService.prepareUserFavoritesModel(any(Model.class), any(UserDetails.class)))
                .thenReturn("user/favorites");


        mockMvc.perform(get("/user/favorites"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/favorites"));
        
        verify(userService, times(1)).prepareUserFavoritesModel(any(Model.class), any(UserDetails.class));
    }

    @Test
    public void testListFavorites_Unauthenticated_ShouldRedirectToLogin() throws Exception {

        mockMvc.perform(get("/user/favorites"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        
        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testListFavorites_AdminRole_ShouldWork() throws Exception {

        when(userService.prepareUserFavoritesModel(any(Model.class), any(UserDetails.class)))
                .thenReturn("user/favorites");


        mockMvc.perform(get("/user/favorites"))
                .andExpect(status().isOk());
        
        verify(userService, times(1)).prepareUserFavoritesModel(any(Model.class), any(UserDetails.class));
    }
}