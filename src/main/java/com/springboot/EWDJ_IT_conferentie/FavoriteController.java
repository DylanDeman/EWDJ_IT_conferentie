package com.springboot.EWDJ_IT_conferentie;

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
	public String addToFavorites(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {

		return favoriteService.processAddToFavorites(id, userDetails, redirectAttributes);
	}

	@PostMapping("/{id}/unfavorite")
	public String removeFromFavorites(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {

		return favoriteService.processRemoveFromFavorites(id, userDetails, redirectAttributes);
	}
}