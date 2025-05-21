package com.springboot.EWDJ_IT_conferentie;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import service.FavoriteService;

public class FavoriteControllerTest {

    private FavoriteService favoriteService;
    private FavoriteController favoriteController;
    private MockMvc mockMvc;
    private RedirectAttributes redirectAttributes;
    private HttpServletRequest request;

    @BeforeEach
    public void setup() {
        favoriteService = mock(FavoriteService.class);
        favoriteController = new FavoriteController();
        redirectAttributes = mock(RedirectAttributes.class);
        request = mock(HttpServletRequest.class);

        ReflectionTestUtils.setField(favoriteController, "favoriteService", favoriteService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(favoriteController)
                .build();
    }

    @Test
    public void testAddToFavorites_MockMvc_Authenticated() throws Exception {
        Long eventId = 1L;

        mockMvc.perform(post("/events/{id}/favorite", eventId)
                        .flashAttr("username", "testuser"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/" + eventId))
                .andExpect(flash().attribute("message", "Event added to favorites"));

        verify(favoriteService, times(1)).toggleFavorite(eq(eventId), eq("testuser"));
    }

    @Test
    public void testAddToFavorites_MockMvc_Unauthenticated() throws Exception {
        Long eventId = 1L;

        mockMvc.perform(post("/events/{id}/favorite", eventId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("error", "You must be logged in to add favorites."));

        verify(favoriteService, times(0)).toggleFavorite(anyLong(), anyString());
    }

    @Test
    public void testAddToFavorites_MockMvc_ServiceThrowsException() throws Exception {
        Long eventId = 1L;
        String errorMessage = "Error toggling favorite status";

        doThrow(new RuntimeException(errorMessage))
                .when(favoriteService).toggleFavorite(eq(eventId), eq("testuser"));

        mockMvc.perform(post("/events/{id}/favorite", eventId)
                        .flashAttr("username", "testuser"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/" + eventId))
                .andExpect(flash().attribute("error", errorMessage));

        verify(favoriteService, times(1)).toggleFavorite(eq(eventId), eq("testuser"));
    }

    @Test
    public void testRemoveFromFavorites_MockMvc_Authenticated_DefaultRedirect() throws Exception {
        Long eventId = 1L;

        mockMvc.perform(post("/events/{id}/unfavorite", eventId)
                        .flashAttr("username", "testuser"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/" + eventId))
                .andExpect(flash().attribute("message", "Event removed from favorites"));

        verify(favoriteService, times(1)).toggleFavorite(eq(eventId), eq("testuser"));
    }

    @Test
    public void testRemoveFromFavorites_MockMvc_Authenticated_FavoritesPageRedirect() throws Exception {
        Long eventId = 1L;

        mockMvc.perform(post("/events/{id}/unfavorite", eventId)
                        .flashAttr("username", "testuser")
                        .header("Referer", "http://localhost:8080/user/favorites"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/favorites"))
                .andExpect(flash().attribute("message", "Event removed from favorites"));

        verify(favoriteService, times(1)).toggleFavorite(eq(eventId), eq("testuser"));
    }

    @Test
    public void testRemoveFromFavorites_MockMvc_Unauthenticated() throws Exception {
        Long eventId = 1L;

        mockMvc.perform(post("/events/{id}/unfavorite", eventId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("error", "You must be logged in to remove favorites."));

        verify(favoriteService, times(0)).toggleFavorite(anyLong(), anyString());
    }

    @Test
    public void testRemoveFromFavorites_MockMvc_ServiceThrowsException() throws Exception {
        Long eventId = 1L;
        String errorMessage = "Error toggling favorite status";

        doThrow(new RuntimeException(errorMessage))
                .when(favoriteService).toggleFavorite(eq(eventId), eq("testuser"));

        mockMvc.perform(post("/events/{id}/unfavorite", eventId)
                        .flashAttr("username", "testuser"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/" + eventId))
                .andExpect(flash().attribute("error", errorMessage));

        verify(favoriteService, times(1)).toggleFavorite(eq(eventId), eq("testuser"));
    }

    @Test
    public void testDirectAddToFavorites_Success() {
        Long eventId = 1L;
        TestPrincipal principal = new TestPrincipal("testuser");

        String result = favoriteController.addToFavorites(eventId, principal, redirectAttributes);

        verify(favoriteService, times(1)).toggleFavorite(eq(eventId), eq("testuser"));
        verify(redirectAttributes).addFlashAttribute("message", "Event added to favorites");
        assert result.equals("redirect:/events/" + eventId);
    }

    @Test
    public void testDirectAddToFavorites_NoUserDetails() {
        Long eventId = 1L;

        String result = favoriteController.addToFavorites(eventId, null, redirectAttributes);

        verify(redirectAttributes).addFlashAttribute("error", "You must be logged in to add favorites.");
        assert result.equals("redirect:/login");
    }

    @Test
    public void testDirectAddToFavorites_ServiceException() {
        Long eventId = 1L;
        String errorMessage = "Error toggling favorite";
        TestPrincipal principal = new TestPrincipal("testuser");

        when(favoriteService.toggleFavorite(eq(eventId), eq("testuser")))
                .thenThrow(new RuntimeException(errorMessage));

        String result = favoriteController.addToFavorites(eventId, principal, redirectAttributes);

        verify(redirectAttributes).addFlashAttribute("error", errorMessage);
        assert result.equals("redirect:/events/" + eventId);
    }

    @Test
    public void testDirectRemoveFromFavorites_Success() {
        Long eventId = 1L;
        UserDetails principal = mock(UserDetails.class);
        when(principal.getUsername()).thenReturn("testuser");

        String result = favoriteController.removeFromFavorites(eventId, principal, redirectAttributes, request);

        verify(favoriteService, times(1)).toggleFavorite(eq(eventId), eq("testuser"));
        verify(redirectAttributes).addFlashAttribute("message", "Event removed from favorites");
        assert result.equals("redirect:/events/" + eventId);
    }

    @Test
    public void testDirectRemoveFromFavorites_RedirectToFavorites() {
        Long eventId = 1L;
        UserDetails principal = mock(UserDetails.class);
        when(principal.getUsername()).thenReturn("testuser");
        when(request.getHeader("Referer")).thenReturn("http://localhost:8080/user/favorites");

        String result = favoriteController.removeFromFavorites(eventId, principal, redirectAttributes, request);

        verify(favoriteService, times(1)).toggleFavorite(eq(eventId), eq("testuser"));
        verify(redirectAttributes).addFlashAttribute("message", "Event removed from favorites");
        assert result.equals("redirect:/user/favorites");
    }

    @Test
    public void testDirectRemoveFromFavorites_NullReferer() {
        Long eventId = 1L;
        UserDetails principal = mock(UserDetails.class);
        when(principal.getUsername()).thenReturn("testuser");
        when(request.getHeader("Referer")).thenReturn(null);

        String result = favoriteController.removeFromFavorites(eventId, principal, redirectAttributes, request);

        verify(favoriteService, times(1)).toggleFavorite(eq(eventId), eq("testuser"));
        verify(redirectAttributes).addFlashAttribute("message", "Event removed from favorites");
        assert result.equals("redirect:/events/" + eventId);
    }

    @Test
    public void testDirectRemoveFromFavorites_NoUserDetails() {
        Long eventId = 1L;

        String result = favoriteController.removeFromFavorites(eventId, null, redirectAttributes, request);

        verify(redirectAttributes).addFlashAttribute("error", "You must be logged in to remove favorites.");
        assert result.equals("redirect:/login");
    }

    @Test
    public void testDirectRemoveFromFavorites_ServiceException() {
        Long eventId = 1L;
        String errorMessage = "Error toggling favorite";
        UserDetails principal = mock(UserDetails.class);
        when(principal.getUsername()).thenReturn("testuser");

        when(favoriteService.toggleFavorite(eq(eventId), eq("testuser")))
                .thenThrow(new RuntimeException(errorMessage));

        String result = favoriteController.removeFromFavorites(eventId, principal, redirectAttributes, request);

        verify(redirectAttributes).addFlashAttribute("error", errorMessage);
        assert result.equals("redirect:/events/" + eventId);
    }

    public static class TestPrincipal {
        private final String username;

        public TestPrincipal(String username) {
            this.username = username;
        }

        public String getUsername() {
            return username;
        }
    }
}