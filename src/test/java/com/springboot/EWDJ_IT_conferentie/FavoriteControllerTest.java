package com.springboot.EWDJ_IT_conferentie;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import service.FavoriteService;

public class FavoriteControllerTest {

    private FavoriteService favoriteService;
    private FavoriteController favoriteController;
    private RedirectAttributes redirectAttributes;
    private HttpServletRequest request;

    @BeforeEach
    public void setup() {
        favoriteService = mock(FavoriteService.class);
        favoriteController = new FavoriteController();
        redirectAttributes = mock(RedirectAttributes.class);
        request = mock(HttpServletRequest.class);

        ReflectionTestUtils.setField(favoriteController, "favoriteService", favoriteService);
    }

    @Test
    public void testAddToFavorites_Authenticated() {
        Long eventId = 1L;
        UserDetails userDetails = new StubUserDetails("testuser");

        String result = favoriteController.addToFavorites(eventId, userDetails, redirectAttributes);

        verify(favoriteService, times(1)).toggleFavorite(eq(eventId), eq("testuser"));
        verify(redirectAttributes).addFlashAttribute("message", "Event added to favorites");
        assert result.equals("redirect:/events/" + eventId);
    }

    @Test
    public void testAddToFavorites_Unauthenticated() {
        Long eventId = 1L;

        String result = favoriteController.addToFavorites(eventId, null, redirectAttributes);

        verify(favoriteService, times(0)).toggleFavorite(anyLong(), anyString());
        verify(redirectAttributes).addFlashAttribute("error", "You must be logged in to add favorites.");
        assert result.equals("redirect:/login");
    }

    @Test
    public void testAddToFavorites_ServiceThrowsException() {
        Long eventId = 1L;
        String errorMessage = "Error toggling favorite status";
        UserDetails userDetails = new StubUserDetails("testuser");

        doThrow(new RuntimeException(errorMessage))
                .when(favoriteService).toggleFavorite(eq(eventId), eq("testuser"));

        String result = favoriteController.addToFavorites(eventId, userDetails, redirectAttributes);

        verify(favoriteService, times(1)).toggleFavorite(eq(eventId), eq("testuser"));
        verify(redirectAttributes).addFlashAttribute("error", errorMessage);
        assert result.equals("redirect:/events/" + eventId);
    }

    @Test
    public void testRemoveFromFavorites_Authenticated_DefaultRedirect() {
        Long eventId = 1L;
        UserDetails userDetails = new StubUserDetails("testuser");

        String result = favoriteController.removeFromFavorites(eventId, userDetails, redirectAttributes, request);

        verify(favoriteService, times(1)).toggleFavorite(eq(eventId), eq("testuser"));
        verify(redirectAttributes).addFlashAttribute("message", "Event removed from favorites");
        assert result.equals("redirect:/events/" + eventId);
    }

    @Test
    public void testRemoveFromFavorites_Authenticated_FavoritesPageRedirect() {
        Long eventId = 1L;
        UserDetails userDetails = new StubUserDetails("testuser");
        when(request.getHeader("Referer")).thenReturn("http://localhost:8080/user/favorites");

        String result = favoriteController.removeFromFavorites(eventId, userDetails, redirectAttributes, request);

        verify(favoriteService, times(1)).toggleFavorite(eq(eventId), eq("testuser"));
        verify(redirectAttributes).addFlashAttribute("message", "Event removed from favorites");
        assert result.equals("redirect:/user/favorites");
    }

    @Test
    public void testRemoveFromFavorites_NullReferer() {
        Long eventId = 1L;
        UserDetails userDetails = new StubUserDetails("testuser");
        when(request.getHeader("Referer")).thenReturn(null);

        String result = favoriteController.removeFromFavorites(eventId, userDetails, redirectAttributes, request);

        verify(favoriteService, times(1)).toggleFavorite(eq(eventId), eq("testuser"));
        verify(redirectAttributes).addFlashAttribute("message", "Event removed from favorites");
        assert result.equals("redirect:/events/" + eventId);
    }

    @Test
    public void testRemoveFromFavorites_Unauthenticated() {
        Long eventId = 1L;

        String result = favoriteController.removeFromFavorites(eventId, null, redirectAttributes, request);

        verify(favoriteService, times(0)).toggleFavorite(anyLong(), anyString());
        verify(redirectAttributes).addFlashAttribute("error", "You must be logged in to remove favorites.");
        assert result.equals("redirect:/login");
    }

    @Test
    public void testRemoveFromFavorites_ServiceException() {
        Long eventId = 1L;
        String errorMessage = "Error toggling favorite status";
        UserDetails userDetails = new StubUserDetails("testuser");

        doThrow(new RuntimeException(errorMessage))
                .when(favoriteService).toggleFavorite(eq(eventId), eq("testuser"));

        String result = favoriteController.removeFromFavorites(eventId, userDetails, redirectAttributes, request);

        verify(favoriteService, times(1)).toggleFavorite(eq(eventId), eq("testuser"));
        verify(redirectAttributes).addFlashAttribute("error", errorMessage);
        assert result.equals("redirect:/events/" + eventId);
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