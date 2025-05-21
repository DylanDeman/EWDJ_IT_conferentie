package com.springboot.EWDJ_IT_conferentie;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.Model;

import service.UserService;

public class UserControllerTest {

    private UserService userService;
    private UserController userController;
    private Model model;

    @BeforeEach
    public void setup() {
        userService = mock(UserService.class);
        userController = new UserController();
        model = mock(Model.class);

        ReflectionTestUtils.setField(userController, "userService", userService);
    }

    @Test
    public void testListFavorites() {
        UserDetails userDetails = new StubUserDetails("testuser");

        when(userService.prepareUserFavoritesModel(model, userDetails)).thenReturn("user/favorites");

        String viewName = userController.listFavorites(model, userDetails);

        verify(userService).prepareUserFavoritesModel(model, userDetails);
        assert viewName.equals("user/favorites");
    }

    @Test
    public void testListFavorites_AlternativeView() {
        UserDetails userDetails = new StubUserDetails("testuser");

        when(userService.prepareUserFavoritesModel(model, userDetails)).thenReturn("redirect:/login");

        String viewName = userController.listFavorites(model, userDetails);

        verify(userService).prepareUserFavoritesModel(model, userDetails);
        assert viewName.equals("redirect:/login");
    }

    private static class StubUserDetails implements UserDetails {
        private final String username;

        public StubUserDetails(String username) {
            this.username = username;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return Collections.emptyList();
        }

        @Override
        public String getPassword() {
            return "password";
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}