package com.springboot.EWDJ_IT_conferentie;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import domain.Room;
import jakarta.validation.Valid;
import service.RoomService;

@Controller
@RequestMapping("/rooms")
public class RoomController {
	private final RoomService roomService;
	private final MessageSource messageSource;

	public RoomController(RoomService roomService, MessageSource messageSource) {
		this.roomService = roomService;
		this.messageSource = messageSource;
	}

	@GetMapping
	public String listRooms(Model model, @RequestParam(required = false) Integer capacity,
			@RequestParam(required = false) String search,
			@RequestParam(required = false, defaultValue = "name") String sort) {

		// Get all rooms first
		List<Room> rooms = roomService.getAllRooms();

		// Apply capacity filter if provided
		if (capacity != null && capacity > 0) {
			rooms = rooms.stream().filter(room -> room.getCapacity() >= capacity).collect(Collectors.toList());
		}

		// Apply search filter if provided
		if (search != null && !search.trim().isEmpty()) {
			String searchLower = search.toLowerCase();
			rooms = rooms.stream().filter(room -> room.getName().toLowerCase().contains(searchLower))
					.collect(Collectors.toList());
		}

		// Apply sorting
		if ("capacity".equals(sort)) {
			rooms = rooms.stream().sorted(Comparator.comparing(Room::getCapacity).reversed())
					.collect(Collectors.toList());
		} else {
			// Default sort by name
			rooms = rooms.stream().sorted(Comparator.comparing(room -> room.getName().toLowerCase()))
					.collect(Collectors.toList());
		}

		// Add to model
		model.addAttribute("rooms", rooms);

		// Return filter parameters to maintain state
		if (capacity != null) {
			model.addAttribute("capacityFilter", capacity);
		}
		if (search != null) {
			model.addAttribute("searchFilter", search);
		}
		model.addAttribute("sortFilter", sort);

		return "rooms/list";
	}

	@GetMapping("/new")
	public String showCreateForm(Model model) {
		model.addAttribute("room", new Room());
		return "rooms/form";
	}

	@PostMapping("/new")
	public String createRoom(@Valid @ModelAttribute Room room, BindingResult result,
			RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			return "rooms/form";
		}
		try {
			Room savedRoom = roomService.createRoom(room);
			String message = messageSource.getMessage("room.added",
					new Object[] { savedRoom.getName(), savedRoom.getCapacity() }, LocaleContextHolder.getLocale());
			redirectAttributes.addFlashAttribute("message", message);
			return "redirect:/rooms";
		} catch (Exception e) {
			result.rejectValue("name", "error.room", e.getMessage());
			return "rooms/form";
		}
	}

	@PostMapping("/{id}/delete")
	public String deleteRoom(@PathVariable Long id, @RequestParam(required = false) Integer capacity,
			@RequestParam(required = false) String search,
			@RequestParam(required = false, defaultValue = "name") String sort, RedirectAttributes redirectAttributes) {
		try {
			Room room = roomService.getRoomById(id)
					.orElseThrow(() -> new IllegalArgumentException("Invalid room id: " + id));

			// Check if room has scheduled events
			if (!room.getEvents().isEmpty()) {
				String errorMsg = messageSource.getMessage("room.delete.events",
						new Object[] { room.getEvents().size() }, LocaleContextHolder.getLocale());
				redirectAttributes.addFlashAttribute("error", errorMsg);
				return "redirect:/rooms";
			}

			roomService.deleteRoom(id);

			String message = messageSource.getMessage("room.deleted", new Object[] { room.getName() },
					LocaleContextHolder.getLocale());
			redirectAttributes.addFlashAttribute("message", message);
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Error deleting room: " + e.getMessage());
		}

		// Keep filter parameters in the redirect
		String redirect = "redirect:/rooms";
		boolean hasParams = false;

		if (capacity != null) {
			redirect += hasParams ? "&capacity=" + capacity : "?capacity=" + capacity;
			hasParams = true;
		}

		if (search != null && !search.isEmpty()) {
			redirect += hasParams ? "&search=" + search : "?search=" + search;
			hasParams = true;
		}

		if (sort != null) {
			redirect += hasParams ? "&sort=" + sort : "?sort=" + sort;
		}

		return redirect;
	}

	@GetMapping("/{id}/confirm")
	public String confirmDelete(@PathVariable Long id, Model model, @RequestParam(required = false) Integer capacity,
			@RequestParam(required = false) String search,
			@RequestParam(required = false, defaultValue = "name") String sort) {

		Room room = roomService.getRoomById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid room id: " + id));

		model.addAttribute("room", room);

		// Keep filter parameters
		if (capacity != null) {
			model.addAttribute("capacityFilter", capacity);
		}
		if (search != null) {
			model.addAttribute("searchFilter", search);
		}
		model.addAttribute("sortFilter", sort);

		return "rooms/confirm-delete";
	}
}