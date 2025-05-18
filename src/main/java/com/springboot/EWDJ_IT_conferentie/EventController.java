package com.springboot.EWDJ_IT_conferentie;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import domain.Event;
import domain.Speaker;
import jakarta.validation.Valid;
import service.EventService;
import service.RoomService;
import service.SpeakerService;
import service.UserService;
import util.Role;

@Controller
@RequestMapping("/events")
public class EventController {

	@Autowired
	private EventService eventService;

	@Autowired
	private RoomService roomService;

	@Autowired
	private UserService userService;

	@Autowired
	private SpeakerService speakerService;

	@GetMapping({ "", "/" })
	public String listEvents(Model model, @RequestParam(required = false) String date,
			@RequestParam(required = false) Long room,
			@RequestParam(required = false, defaultValue = "datetime") String sort,
			@AuthenticationPrincipal UserDetails userDetails) {

		List<Event> events = eventService.getFilteredEvents(date, room, sort);
		model.addAttribute("events", events);
		model.addAttribute("rooms", roomService.getAllRooms());

		if (userDetails != null) {
			model.addAttribute("isAdmin", isAdmin(userDetails.getUsername()));
			model.addAttribute("userFavorites", userService.findByUsername(userDetails.getUsername()).getFavorites());
		}

		return "events/list";
	}

	@GetMapping("/{id}")
	public String viewEvent(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
		Event event = eventService.getEventById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid event id: " + id));

		model.addAttribute("event", event);
		if (userDetails != null) {
			model.addAttribute("isAdmin", isAdmin(userDetails.getUsername()));
			model.addAttribute("canAddToFavorites", !eventService.hasReachedFavoriteLimit(userDetails.getUsername()));
		}
		return "events/view";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/new")
	public String showCreateForm(Model model) {
		Event event = new Event();
		event.setSpeakers(new ArrayList<>());
		model.addAttribute("event", event);
		model.addAttribute("rooms", roomService.getAllRooms());
		model.addAttribute("allSpeakers", speakerService.findAll());
		return "events/form";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/{id}/edit")
	public String showEditForm(@PathVariable Long id, Model model) {
		Event event = eventService.getEventById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid event id: " + id));

		if (event.getSpeakers() == null) {
			event.setSpeakers(new ArrayList<>());
		}
		model.addAttribute("event", event);
		model.addAttribute("rooms", roomService.getAllRooms());
		model.addAttribute("allSpeakers", speakerService.findAll());
		return "events/form";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping({ "", "/" })
	public String createEvent(@Valid @ModelAttribute Event event, BindingResult result,
			@RequestParam(required = false) String action, Model model, RedirectAttributes redirectAttributes) {

		convertSpeakerIdsToEntities(event);

		if (result.hasErrors()) {
			populateModelForForm(model);
			return "events/form";
		}

		try {
			eventService.createEvent(event);
			redirectAttributes.addFlashAttribute("message", "Event created successfully");
			return "redirect:/events";
		} catch (Exception e) {
			result.rejectValue("", "", e.getMessage());
			populateModelForForm(model);
			return "events/form";
		}
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/{id}")
	public String updateEvent(@PathVariable Long id, @Valid @ModelAttribute Event event, BindingResult result,
			@RequestParam(required = false) String action, Model model, RedirectAttributes redirectAttributes) {

		convertSpeakerIdsToEntities(event);

		if (result.hasErrors()) {
			populateModelForForm(model);
			return "events/form";
		}

		try {
			eventService.updateEvent(id, event);
			redirectAttributes.addFlashAttribute("message", "Event updated successfully");
			return "redirect:/events";
		} catch (Exception e) {
			result.rejectValue("", "", e.getMessage());
			populateModelForForm(model);
			return "events/form";
		}
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/{id}/delete")
	public String deleteEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		eventService.deleteEvent(id);
		redirectAttributes.addFlashAttribute("message", "Event deleted successfully");
		return "redirect:/events";
	}

	@PostMapping("/{id}/favorite")
	public String addToFavorites(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {
		if (userDetails == null) {
			redirectAttributes.addFlashAttribute("error", "You must be logged in to add favorites.");
			return "redirect:/login";
		}
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
		if (userDetails == null) {
			redirectAttributes.addFlashAttribute("error", "You must be logged in to remove favorites.");
			return "redirect:/login";
		}
		eventService.removeFromFavorites(id, userDetails.getUsername());
		redirectAttributes.addFlashAttribute("message", "Event removed from favorites");
		return "redirect:/events/" + id;
	}

	private void convertSpeakerIdsToEntities(Event event) {
		List<Speaker> realSpeakers = new ArrayList<>();
		if (event.getSpeakers() != null) {
			for (Speaker s : event.getSpeakers()) {
				if (s.getId() != null) {
					realSpeakers.add(speakerService.findById(s.getId()));
				}
			}
		}
		event.setSpeakers(realSpeakers);
	}

	private void populateModelForForm(Model model) {
		model.addAttribute("rooms", roomService.getAllRooms());
		model.addAttribute("allSpeakers", speakerService.findAll());
	}

	private boolean isAdmin(String username) {
		return userService.findByUsername(username).getRole() == Role.ADMIN;
	}
}
