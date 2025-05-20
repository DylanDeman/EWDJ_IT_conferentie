package com.springboot.EWDJ_IT_conferentie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserService userService;

	@GetMapping("/favorites")
	@PreAuthorize("hasRole('USER')")
	public String listFavorites(Model model, @AuthenticationPrincipal UserDetails userDetails) {
		return userService.prepareUserFavoritesModel(model, userDetails);
	}
}