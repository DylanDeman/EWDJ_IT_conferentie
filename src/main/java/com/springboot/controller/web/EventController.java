package com.springboot.controller.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springboot.service.EventService;
import com.springboot.service.UserService;

import domain.Event;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/events")
public class EventController {

	private final EventService eventService;
	private final UserService userService;

	public EventController(EventService eventService, UserService userService) {
		this.eventService = eventService;
		this.userService = userService;
	}

	@GetMapping
	public String listEvents(Model model, @AuthenticationPrincipal UserDetails userDetails) {
		model.addAttribute("events", eventService.getAllEvents());
		if (userDetails != null) {
			model.addAttribute("isAdmin", userService.isAdmin(userDetails.getUsername()));
		}
		return "events/list";
	}

	@GetMapping("/{id}")
	public String viewEvent(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
		Event event = eventService.getEventById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid event id: " + id));

		model.addAttribute("event", event);
		if (userDetails != null) {
			model.addAttribute("isAdmin", userService.isAdmin(userDetails.getUsername()));
			model.addAttribute("canAddToFavorites", !eventService.hasReachedFavoriteLimit(userDetails.getUsername()));
		}
		return "events/view";
	}

	@GetMapping("/new")
	public String showCreateForm(Model model) {
		model.addAttribute("event", new Event());
		return "events/form";
	}

	@PostMapping
	public String createEvent(@Valid @ModelAttribute Event event, BindingResult result,
			RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			return "events/form";
		}

		try {
			eventService.createEvent(event);
			redirectAttributes.addFlashAttribute("message", "Event created successfully");
			return "redirect:/events";
		} catch (Exception e) {
			result.rejectValue("", "", e.getMessage());
			return "events/form";
		}
	}

	@GetMapping("/{id}/edit")
	public String showEditForm(@PathVariable Long id, Model model) {
		Event event = eventService.getEventById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid event id: " + id));
		model.addAttribute("event", event);
		return "events/form";
	}

	@PostMapping("/{id}")
	public String updateEvent(@PathVariable Long id, @Valid @ModelAttribute Event event, BindingResult result,
			RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			return "events/form";
		}

		try {
			eventService.updateEvent(id, event);
			redirectAttributes.addFlashAttribute("message", "Event updated successfully");
			return "redirect:/events";
		} catch (Exception e) {
			result.rejectValue("", "", e.getMessage());
			return "events/form";
		}
	}

	@PostMapping("/{id}/delete")
	public String deleteEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		eventService.deleteEvent(id);
		redirectAttributes.addFlashAttribute("message", "Event deleted successfully");
		return "redirect:/events";
	}

	@PostMapping("/{id}/favorite")
	public String addToFavorites(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {
		try {
			eventService.addToFavorites(id, userDetails.getUsername());
			redirectAttributes.addFlashAttribute("message", "Event added to favorites");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/events/" + id;
	}

	@PostMapping("/{id}/unfavorite")
	public String removeFromFavorites(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {
		eventService.removeFromFavorites(id, userDetails.getUsername());
		redirectAttributes.addFlashAttribute("message", "Event removed from favorites");
		return "redirect:/events/" + id;
	}
}