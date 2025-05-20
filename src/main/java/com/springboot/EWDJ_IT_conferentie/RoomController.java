package com.springboot.EWDJ_IT_conferentie;

import org.springframework.context.MessageSource;
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

		roomService.prepareRoomListModel(model, capacity, search, sort);
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

		return roomService.processRoomCreation(room, result, redirectAttributes, messageSource);
	}

	@PostMapping("/{id}/delete")
	public String deleteRoom(@PathVariable Long id, @RequestParam(required = false) Integer capacity,
			@RequestParam(required = false) String search,
			@RequestParam(required = false, defaultValue = "name") String sort, RedirectAttributes redirectAttributes) {

		return roomService.processRoomDeletion(id, capacity, search, sort, redirectAttributes, messageSource);
	}

	@GetMapping("/{id}/confirm")
	public String confirmDelete(@PathVariable Long id, Model model, @RequestParam(required = false) Integer capacity,
			@RequestParam(required = false) String search,
			@RequestParam(required = false, defaultValue = "name") String sort) {

		return roomService.prepareDeleteConfirmation(id, model, capacity, search, sort);
	}
}