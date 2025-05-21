package com.springboot.EWDJ_IT_conferentie;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.FavoriteService;

@Controller
@RequestMapping("/events")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/{id}/favorite")
    public String addToFavorites(@PathVariable Long id, 
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to add favorites.");
            return "redirect:/login";
        }

        try {
            favoriteService.toggleFavorite(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("message", "Event added to favorites");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/events/" + id;
    }

    @PostMapping("/{id}/unfavorite")
    public String removeFromFavorites(@PathVariable Long id, 
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes,
                                    HttpServletRequest request) {
        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to remove favorites.");
            return "redirect:/login";
        }

        try {
            favoriteService.toggleFavorite(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("message", "Event removed from favorites");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/user/favorites")) {
            return "redirect:/user/favorites";
        }
        
        return "redirect:/events/" + id;
    }
}