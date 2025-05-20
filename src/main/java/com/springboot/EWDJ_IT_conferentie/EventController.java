package com.springboot.EWDJ_IT_conferentie;

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
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import service.EventService;
import service.UserService;

@Controller
@RequestMapping("/events")
public class EventController {

	@Autowired
	private EventService eventService;

	@Autowired
	private UserService userService;

	@GetMapping({ "", "/" })
	public String listEvents(Model model, @RequestParam(required = false) String date,
			@RequestParam(required = false) Long room,
			@RequestParam(required = false, defaultValue = "datetime") String sort,
			@AuthenticationPrincipal UserDetails userDetails) {

		model.addAttribute("events", eventService.getFilteredEvents(date, room, sort));
		model.addAttribute("rooms", eventService.setupRooms());

		if (userDetails != null) {
			String username = userDetails.getUsername();
			model.addAttribute("isAdmin", userService.isAdmin(username));
			model.addAttribute("userFavorites", eventService.getUserFavorites(username));
		}

		return "events/list";
	}

	@GetMapping("/{id}")
	public String viewEvent(@PathVariable Long id, Model model, HttpSession session,
			@AuthenticationPrincipal UserDetails userDetails) {
		try {
			eventService.getEventById(id).ifPresentOrElse(event -> model.addAttribute("event", event), () -> {
				throw new IllegalArgumentException("Invalid event id: " + id);
			});

			if (userDetails != null && userService.isAdmin(userDetails.getUsername())) {
				String returnUrl = (String) session.getAttribute("adminEventsUrl");
				if (returnUrl != null) {
					model.addAttribute("returnUrl", returnUrl);
				}
			}

			if (userDetails != null) {
				String username = userDetails.getUsername();
				eventService.prepareUserEventContext(model, username, id);
			} else {
				model.addAttribute("isFavorite", false);
			}

			return "events/detail";
		} catch (IllegalArgumentException e) {
			model.addAttribute("error", e.getMessage());
			return "error";
		} catch (Exception e) {
			model.addAttribute("error", "An error occurred while retrieving the event details");
			return "error";
		}
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/new")
	public String showAddEventForm(Model model) {
		Event newEvent = eventService.createEmptyEvent();
		model.addAttribute("event", newEvent);
		model.addAttribute("beamerCheck", "00");
		eventService.populateEventFormModel(model, newEvent);
		return "events/form";
	}

	@PostMapping("/new")
	public String addEvent(@Valid @ModelAttribute Event event, BindingResult result,
			@RequestParam(required = false) Long speaker1Id, @RequestParam(required = false) Long speaker2Id,
			@RequestParam(required = false) Long speaker3Id, @RequestParam Long roomId,
			@RequestParam(required = false) String beamerCheck, @RequestParam(required = false) String action,
			Model model, RedirectAttributes redirectAttributes) {

		if ("calculate".equals(action)) {
			return eventService.handleCalculateAction(event, roomId, speaker1Id, speaker2Id, speaker3Id, model);
		}

		// Handle form submission
		String validationResult = eventService.validateAndPrepareEvent(event, result, roomId, speaker1Id, speaker2Id,
				speaker3Id, beamerCheck, model);

		if (validationResult != null) {
			return validationResult;
		}

		try {
			eventService.createEvent(event);
			redirectAttributes.addFlashAttribute("message", "Event created successfully");
			return "redirect:/admin/events";
		} catch (Exception e) {
			result.rejectValue(null, null, e.getMessage());
			eventService.populateEventFormModel(model, event);
			model.addAttribute("beamerCheck", beamerCheck);
			return "events/form";
		}
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/{id}/edit")
	public String showEditEventForm(@PathVariable Long id, Model model, HttpSession session) {
		return eventService.prepareEditEventForm(id, model, session);
	}

	@PutMapping("/{id}/edit")
	public String editEvent(@PathVariable Long id, @Valid @ModelAttribute Event event, BindingResult result,
			@RequestParam Long roomId, @RequestParam(required = false) Long speaker1Id,
			@RequestParam(required = false) Long speaker2Id, @RequestParam(required = false) Long speaker3Id,
			@RequestParam String beamerCheck, @RequestParam(required = false) String action, Model model,
			RedirectAttributes redirectAttributes) {

		if ("calculate".equals(action)) {
			return eventService.handleCalculateAction(event, roomId, speaker1Id, speaker2Id, speaker3Id, model);
		}

		if ("addSpeaker".equals(action)) {
			return eventService.handleAddSpeakerAction(event, speaker1Id, speaker2Id, speaker3Id, beamerCheck, model);
		}

		String validationResult = eventService.validateAndPrepareEventForUpdate(id, event, result, roomId, speaker1Id,
				speaker2Id, speaker3Id, beamerCheck, model);

		if (validationResult != null) {
			return validationResult;
		}

		try {
			eventService.updateEvent(id, event);
			redirectAttributes.addFlashAttribute("message", "Event updated successfully");
			return "redirect:/admin/events";
		} catch (Exception e) {
			result.rejectValue("", "", e.getMessage());
			eventService.populateEventFormModel(model, event);
			model.addAttribute("beamerCheck", beamerCheck);
			return "events/form";
		}
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/{id}/delete")
	public String deleteEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			eventService.deleteEvent(id);
			redirectAttributes.addFlashAttribute("message", "Event deleted successfully");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Error deleting event: " + e.getMessage());
		}
		return "redirect:/events";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/{id}/confirm")
	public String confirmDeleteEvent(@PathVariable Long id, Model model, HttpSession session) {
		return eventService.prepareConfirmDeleteEvent(id, model, session);
	}
}