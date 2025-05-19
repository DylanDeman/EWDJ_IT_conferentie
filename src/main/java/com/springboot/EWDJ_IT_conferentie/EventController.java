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
import domain.Room;
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
	private SpeakerService speakerService;

	@Autowired
	private UserService userService;

	// LIST + VIEW EVENTS
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

	// ADD EVENT
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/new")
	public String showAddEventForm(Model model) {
		Event event = new Event();
		event.setSpeakers(new ArrayList<>());
		model.addAttribute("event", event);
		populateModelForForm(model);
		addSpeakerSelectionAttributes(model, event);
		return "events/form";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/new")
	public String addEvent(@Valid @ModelAttribute Event event, BindingResult result,
			@RequestParam(required = false) Long speaker1Id, @RequestParam(required = false) Long speaker2Id,
			@RequestParam(required = false) Long speaker3Id, @RequestParam Long roomId, Model model,
			RedirectAttributes redirectAttributes) {

		Room room = roomService.findById(roomId)
				.orElseThrow(() -> new IllegalArgumentException("Room not found with id: " + roomId));
		event.setRoom(room);

		setSpeakers(event, speaker1Id, speaker2Id, speaker3Id);

		if (result.hasErrors()) {
			populateModelForForm(model);
			addSpeakerSelectionAttributes(model, event);
			return "events/form";
		}

		try {
			eventService.createEvent(event);
			redirectAttributes.addFlashAttribute("message", "Event created successfully");
			return "redirect:/events";
		} catch (Exception e) {
			result.rejectValue(null, null, e.getMessage());
			populateModelForForm(model);
			addSpeakerSelectionAttributes(model, event);
			return "events/form";
		}
	}

	// EDIT EVENT
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/{id}/edit")
	public String showEditEventForm(@PathVariable Long id, Model model) {
		Event event = eventService.getEventById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid event id: " + id));

		if (event.getSpeakers() == null) {
			event.setSpeakers(new ArrayList<>());
		}

		model.addAttribute("event", event);
		populateModelForForm(model);
		addSpeakerSelectionAttributes(model, event);
		return "events/form";
	}

	@PutMapping("/{id}/edit")
	public String editEvent(@PathVariable Long id, @Valid @ModelAttribute Event event, BindingResult result,
			@RequestParam Long roomId, @RequestParam(required = false) Long speaker1Id,
			@RequestParam(required = false) Long speaker2Id, @RequestParam(required = false) Long speaker3Id,
			@RequestParam(required = false) String action, Model model, RedirectAttributes redirectAttributes) {

		// Set the Room on event before validation or save
		Room room = roomService.findById(roomId)
				.orElseThrow(() -> new IllegalArgumentException("Room not found with id:" + roomId));
		event.setRoom(room);

		// Set speakers like you already do
		setSpeakers(event, speaker1Id, speaker2Id, speaker3Id);

		if ("addSpeaker".equals(action)) {
			if (event.getSpeakers().size() < 3) {
				event.getSpeakers().add(new Speaker());
			}
			populateModelForForm(model);
			addSpeakerSelectionAttributes(model, event);
			return "events/form";
		}

		if (result.hasErrors()) {
			populateModelForForm(model);
			addSpeakerSelectionAttributes(model, event);
			return "events/form";
		}

		try {
			eventService.updateEvent(id, event);
			redirectAttributes.addFlashAttribute("message", "Event updated successfully");
			return "redirect:/events";
		} catch (Exception e) {
			result.rejectValue("", "", e.getMessage());
			populateModelForForm(model);
			addSpeakerSelectionAttributes(model, event);
			return "events/form";
		}
	}

	// DELETE EVENT
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/{id}/delete")
	public String deleteEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		eventService.deleteEvent(id);
		redirectAttributes.addFlashAttribute("message", "Event deleted successfully");
		return "redirect:/events";
	}

	// FAVORITES
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

	// UTILS
	private boolean isAdmin(String username) {
		return userService.findByUsername(username).getRole() == Role.ADMIN;
	}

	private void populateModelForForm(Model model) {
		model.addAttribute("rooms", roomService.getAllRooms());
		model.addAttribute("allSpeakers", speakerService.findAll());
	}

	private void addSpeakerSelectionAttributes(Model model, Event event) {
		List<Speaker> speakers = event.getSpeakers();
		model.addAttribute("speaker1", speakers.size() > 0 ? speakers.get(0) : null);
		model.addAttribute("speaker2", speakers.size() > 1 ? speakers.get(1) : null);
		model.addAttribute("speaker3", speakers.size() > 2 ? speakers.get(2) : null);
	}

	private void setSpeakers(Event event, Long speaker1Id, Long speaker2Id, Long speaker3Id) {
		List<Speaker> speakers = new ArrayList<>();
		if (speaker1Id != null) {
			Speaker s1 = speakerService.findById(speaker1Id);
			if (s1 != null)
				speakers.add(s1);
		}
		if (speaker2Id != null && !speaker2Id.equals(speaker1Id)) {
			Speaker s2 = speakerService.findById(speaker2Id);
			if (s2 != null)
				speakers.add(s2);
		}
		if (speaker3Id != null && speakers.stream().noneMatch(s -> s.getId().equals(speaker3Id))) {
			Speaker s3 = speakerService.findById(speaker3Id);
			if (s3 != null)
				speakers.add(s3);
		}
		event.setSpeakers(speakers);
	}
}
