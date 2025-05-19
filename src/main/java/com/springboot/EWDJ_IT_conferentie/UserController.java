package com.springboot.EWDJ_IT_conferentie;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import domain.Event;
import domain.MyUser;
import service.EventService;
import service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;

	public UserController(EventService eventService, UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/favorites")
	@PreAuthorize("hasRole('USER')")
	public String listFavorites(Model model, @AuthenticationPrincipal UserDetails userDetails) {
		if (userDetails == null) {
			return "redirect:/login";
		}

		MyUser user = userService.findByUsername(userDetails.getUsername());

		// Sort favorites by time ascending, then by title alphabetically for same times
		List<Event> sortedFavorites = user.getFavorites().stream()
				.sorted(Comparator.comparing(Event::getDateTime).thenComparing(Event::getName))
				.collect(Collectors.toList());

		model.addAttribute("favorites", sortedFavorites);

		return "user/favorites";
	}
}