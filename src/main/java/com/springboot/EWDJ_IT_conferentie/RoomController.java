package com.springboot.EWDJ_IT_conferentie;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import domain.Room;
import jakarta.validation.Valid;
import service.RoomService;

@Controller
@RequestMapping("/rooms") // <-- Changed here to /rooms
public class RoomController {

	private final RoomService roomService;
	private final MessageSource messageSource;

	public RoomController(RoomService roomService, MessageSource messageSource) {
		this.roomService = roomService;
		this.messageSource = messageSource;
	}

	@GetMapping
	public String listRooms(Model model) {
		model.addAttribute("rooms", roomService.getAllRooms());
		return "rooms/list";
	}

	@GetMapping("/new")
	public String showCreateForm(Model model) {
		model.addAttribute("room", new Room());
		return "rooms/form";
	}

	@PostMapping
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
			result.rejectValue("", "", e.getMessage());
			return "rooms/form";
		}
	}

	@GetMapping("/{id}/edit")
	public String showEditForm(@PathVariable Long id, Model model) {
		Room room = roomService.getRoomById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid room id: " + id));
		model.addAttribute("room", room);
		return "rooms/form";
	}

	@PostMapping("/{id}")
	public String updateRoom(@PathVariable Long id, @Valid @ModelAttribute Room room, BindingResult result,
			RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			return "rooms/form";
		}

		try {
			roomService.updateRoom(id, room);
			redirectAttributes.addFlashAttribute("message", "Room updated successfully");
			return "redirect:/rooms";
		} catch (Exception e) {
			result.rejectValue("", "", e.getMessage());
			return "rooms/form";
		}
	}

	@PostMapping("/{id}/delete")
	public String deleteRoom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		roomService.deleteRoom(id);
		redirectAttributes.addFlashAttribute("message", "Room deleted successfully");
		return "redirect:/rooms";
	}
}
