package com.springboot.EWDJ_IT_conferentie;

import java.time.LocalDate;
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
import domain.MyUser;
import domain.Room;
import domain.Speaker;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import service.EventService;
import service.RoomService;
import service.SpeakerService;
import service.UserService;
import util.Role;
import validation.EventValidator;

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

	@Autowired
	private EventValidator eventValidator;

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
	public String viewEvent(@PathVariable Long id, Model model, HttpSession session,
			@AuthenticationPrincipal UserDetails userDetails) {
		try {
			Event event = eventService.getEventById(id)
					.orElseThrow(() -> new IllegalArgumentException("Invalid event id: " + id));

			model.addAttribute("event", event);

			if (userDetails != null && isAdmin(userDetails.getUsername())) {
				String returnUrl = (String) session.getAttribute("adminEventsUrl");
				if (returnUrl != null) {
					model.addAttribute("returnUrl", returnUrl);
				}
			}

			if (userDetails != null) {
				MyUser user = userService.findByUsername(userDetails.getUsername());
				model.addAttribute("isAdmin", user.getRole() == Role.ADMIN);
				model.addAttribute("canAddToFavorites",
						!eventService.hasReachedFavoriteLimit(userDetails.getUsername()));
				model.addAttribute("isFavorite", user.getFavorites().stream().anyMatch(e -> e.getId().equals(id)));
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
		Event event = new Event();
		event.setSpeakers(new ArrayList<>());
		model.addAttribute("event", event);
		model.addAttribute("beamerCheck", "00");
		populateModelForForm(model);
		addSpeakerSelectionAttributes(model, event);
		return "events/form";
	}

	@PostMapping("/new")
	public String addEvent(@Valid @ModelAttribute Event event, BindingResult result,
			@RequestParam(required = false) Long speaker1Id, @RequestParam(required = false) Long speaker2Id,
			@RequestParam(required = false) Long speaker3Id, @RequestParam Long roomId,
			@RequestParam(required = false) String beamerCheck, @RequestParam(required = false) String action,
			Model model, RedirectAttributes redirectAttributes) {

		if ("calculate".equals(action)) {
			Room room = roomService.findById(roomId).orElse(null);
			if (room != null) {
				event.setRoom(room);
			}

			int calculatedCheck = event.getBeamerCode() % 97;
			model.addAttribute("beamerCheck", String.format("%02d", calculatedCheck));

			model.addAttribute("speaker1Id", speaker1Id);
			model.addAttribute("speaker2Id", speaker2Id);
			model.addAttribute("speaker3Id", speaker3Id);

			setSpeakers(event, speaker1Id, speaker2Id, speaker3Id);

			populateModelForForm(model);
			addSpeakerSelectionAttributes(model, event);
			return "events/form";
		}

		Room room = roomService.findById(roomId)
				.orElseThrow(() -> new IllegalArgumentException("Room not found with id: " + roomId));
		event.setRoom(room);

		setSpeakers(event, speaker1Id, speaker2Id, speaker3Id);

		// Validate name uniqueness for the event date
		if (event.getDateTime() != null && event.getName() != null && !event.getName().isEmpty()) {
			LocalDate eventDate = event.getDateTime().toLocalDate();
			if (eventService.existsByNameAndDate(event.getName(), eventDate)) {
				result.rejectValue("name", "error.name.exists",
						"An event with this name already exists on the same date");
			}
		}

		int calculatedCheck = event.getBeamerCode() % 97;
		if (beamerCheck == null || !beamerCheck.equals(String.format("%02d", calculatedCheck))) {
			model.addAttribute("beamerCheckError", "Invalid checksum. Expected: " + calculatedCheck);
			model.addAttribute("beamerCheck", String.format("%02d", calculatedCheck));

			model.addAttribute("speaker1Id", speaker1Id);
			model.addAttribute("speaker2Id", speaker2Id);
			model.addAttribute("speaker3Id", speaker3Id);

			populateModelForForm(model);
			addSpeakerSelectionAttributes(model, event);
			return "events/form";
		}

		boolean hasSpeaker = (speaker1Id != null && speaker1Id != -1) || (speaker2Id != null && speaker2Id != -1)
				|| (speaker3Id != null && speaker3Id != -1);

		if (!hasSpeaker) {
			model.addAttribute("speakerError", "At least one speaker must be selected.");

			model.addAttribute("speaker1Id", speaker1Id);
			model.addAttribute("speaker2Id", speaker2Id);
			model.addAttribute("speaker3Id", speaker3Id);

			model.addAttribute("beamerCheck", String.format("%02d", calculatedCheck));
			populateModelForForm(model);
			addSpeakerSelectionAttributes(model, event);
			return "events/form";
		}

		if (result.hasErrors()) {
			model.addAttribute("speaker1Id", speaker1Id);
			model.addAttribute("speaker2Id", speaker2Id);
			model.addAttribute("speaker3Id", speaker3Id);
			model.addAttribute("beamerCheck", String.format("%02d", calculatedCheck));
			populateModelForForm(model);
			addSpeakerSelectionAttributes(model, event);
			return "events/form";
		}

		try {
			eventService.createEvent(event);
			redirectAttributes.addFlashAttribute("message", "Event created successfully");
			return "redirect:/admin/events";
		} catch (Exception e) {
			result.rejectValue(null, null, e.getMessage());
			populateModelForForm(model);
			addSpeakerSelectionAttributes(model, event);
			model.addAttribute("beamerCheck", beamerCheck);
			return "events/form";
		}
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/{id}/edit")
	public String showEditEventForm(@PathVariable Long id, Model model, HttpSession session) {
		Event event = eventService.getEventById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid event id: " + id));

		String returnUrl = (String) session.getAttribute("adminEventsUrl");
		if (returnUrl != null) {
			model.addAttribute("returnUrl", returnUrl);
		}

		if (event.getSpeakers() == null) {
			event.setSpeakers(new ArrayList<>());
		}

		int calculatedCheck = event.getBeamerCode() % 97;
		model.addAttribute("beamerCheck", String.format("%02d", calculatedCheck));

		model.addAttribute("event", event);
		populateModelForForm(model);
		addSpeakerSelectionAttributes(model, event);
		return "events/form";
	}

	@PutMapping("/{id}/edit")
	public String editEvent(@PathVariable Long id, @Valid @ModelAttribute Event event, BindingResult result,
			@RequestParam Long roomId, @RequestParam(required = false) Long speaker1Id,
			@RequestParam(required = false) Long speaker2Id, @RequestParam(required = false) Long speaker3Id,
			@RequestParam String beamerCheck, @RequestParam(required = false) String action, Model model,
			RedirectAttributes redirectAttributes) {

		Room room = roomService.findById(roomId)
				.orElseThrow(() -> new IllegalArgumentException("Room not found with id:" + roomId));
		event.setRoom(room);

		if ("calculate".equals(action)) {
			int calculatedCheck = event.getBeamerCode() % 97;
			model.addAttribute("beamerCheck", String.format("%02d", calculatedCheck));

			model.addAttribute("speaker1Id", speaker1Id);
			model.addAttribute("speaker2Id", speaker2Id);
			model.addAttribute("speaker3Id", speaker3Id);

			setSpeakers(event, speaker1Id, speaker2Id, speaker3Id);

			populateModelForForm(model);
			addSpeakerSelectionAttributes(model, event);
			return "events/form";
		}

		if ("addSpeaker".equals(action)) {
			if (event.getSpeakers() == null) {
				event.setSpeakers(new ArrayList<>());
			}
			if (event.getSpeakers().size() < 3) {
				event.getSpeakers().add(new Speaker());
			}

			model.addAttribute("speaker1Id", speaker1Id);
			model.addAttribute("speaker2Id", speaker2Id);
			model.addAttribute("speaker3Id", speaker3Id);

			populateModelForForm(model);
			addSpeakerSelectionAttributes(model, event);
			model.addAttribute("beamerCheck", beamerCheck);
			return "events/form";
		}

		eventValidator.validate(event, result);

		if (event.getDateTime() != null) {
			LocalDate eventDate = event.getDateTime().toLocalDate();
			// Check for duplicate name on same date (exclude current event)
			if (eventService.existsByNameAndDateExcludingId(event.getName(), eventDate, id)) {
				result.rejectValue("name", "error.name.exists",
						"An event with this name already exists on the same date");
			}
		}

		int calculatedCheck = event.getBeamerCode() % 97;
		if (beamerCheck == null || !beamerCheck.equals(String.format("%02d", calculatedCheck))) {
			model.addAttribute("beamerCheckError", "Invalid checksum. Expected: " + calculatedCheck);
			model.addAttribute("beamerCheck", String.format("%02d", calculatedCheck));

			model.addAttribute("speaker1Id", speaker1Id);
			model.addAttribute("speaker2Id", speaker2Id);
			model.addAttribute("speaker3Id", speaker3Id);

			populateModelForForm(model);
			addSpeakerSelectionAttributes(model, event);
			return "events/form";
		}

		boolean hasSpeaker = (speaker1Id != null && speaker1Id != -1) || (speaker2Id != null && speaker2Id != -1)
				|| (speaker3Id != null && speaker3Id != -1);

		if (!hasSpeaker) {
			model.addAttribute("speakerError", "At least one speaker must be selected.");

			model.addAttribute("speaker1Id", speaker1Id);
			model.addAttribute("speaker2Id", speaker2Id);
			model.addAttribute("speaker3Id", speaker3Id);

			model.addAttribute("beamerCheck", String.format("%02d", calculatedCheck));
			populateModelForForm(model);
			addSpeakerSelectionAttributes(model, event);
			return "events/form";
		}

		setSpeakers(event, speaker1Id, speaker2Id, speaker3Id);

		if (result.hasErrors()) {
			populateModelForForm(model);
			addSpeakerSelectionAttributes(model, event);
			model.addAttribute("beamerCheck", beamerCheck);
			model.addAttribute("speaker1Id", speaker1Id);
			model.addAttribute("speaker2Id", speaker2Id);
			model.addAttribute("speaker3Id", speaker3Id);
			return "events/form";
		}

		try {
			eventService.updateEvent(id, event);
			redirectAttributes.addFlashAttribute("message", "Event updated successfully");
			return "redirect:/admin/events";
		} catch (Exception e) {
			result.rejectValue("", "", e.getMessage());
			populateModelForForm(model);
			addSpeakerSelectionAttributes(model, event);
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

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/{id}/confirm")
	public String confirmDeleteEvent(@PathVariable Long id, Model model, HttpSession session) {
		try {
			Event event = eventService.getEventById(id)
					.orElseThrow(() -> new IllegalArgumentException("Invalid event id: " + id));

			model.addAttribute("event", event);

			String returnUrl = (String) session.getAttribute("adminEventsUrl");
			if (returnUrl != null) {
				model.addAttribute("returnUrl", returnUrl);
			}

			return "events/confirm-delete";
		} catch (Exception e) {
			model.addAttribute("error", "Error retrieving event: " + e.getMessage());
			return "error";
		}
	}
}