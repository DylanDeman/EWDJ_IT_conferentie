package service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import domain.Event;
import domain.Room;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import repository.RoomRepository;

@Service
@Slf4j
public class RoomServiceImpl implements RoomService {
	@Autowired
	private RoomRepository roomRepository;

	@Override
	@Transactional(readOnly = true)
	public List<Room> getAllRooms() {
		return roomRepository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Room> getRoomById(Long id) {
		return roomRepository.findById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Room> getRoomByName(String name) {
		return roomRepository.findByName(name);
	}

	@Override
	public Room createRoom(Room room) {
		validateRoom(room);
		return roomRepository.save(room);
	}

	@Override
	public void deleteRoom(Long id) {
		roomRepository.deleteById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Event> getRoomEvents(Long roomId) {
		return roomRepository.findEventsByRoomId(roomId);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByName(String name) {
		return roomRepository.existsByName(name);
	}

	@Override
	@Transactional(readOnly = true)
	public int getRoomCapacity(Long roomId) {
		return roomRepository.findById(roomId).map(Room::getCapacity)
				.orElseThrow(() -> new EntityNotFoundException("Room not found"));
	}

	private void validateRoom(Room room) {
		if (room.getCapacity() < 0 || room.getCapacity() > 50) {
			throw new IllegalArgumentException("Room capacity must be between 0 and 50");
		}
		if (!room.getName().matches("^[A-Z]\\d{3}$")) {
			throw new IllegalArgumentException("Room name must start with a letter followed by 3 digits");
		}
		if (existsByName(room.getName())) {
			throw new IllegalArgumentException("Room name must be unique");
		}
	}

	@Override
	public void prepareRoomListModel(Model model, Integer capacity, String search, String sort) {
		List<Room> rooms = getAllRooms();

		if (capacity != null && capacity > 0) {
			rooms = rooms.stream().filter(room -> room.getCapacity() >= capacity).collect(Collectors.toList());
		}

		if (search != null && !search.trim().isEmpty()) {
			String searchLower = search.toLowerCase();
			rooms = rooms.stream().filter(room -> room.getName().toLowerCase().contains(searchLower))
					.collect(Collectors.toList());
		}

		if ("capacity".equals(sort)) {
			rooms = rooms.stream().sorted(Comparator.comparing(Room::getCapacity).reversed())
					.collect(Collectors.toList());
		} else {
			rooms = rooms.stream().sorted(Comparator.comparing(room -> room.getName().toLowerCase()))
					.collect(Collectors.toList());
		}

		model.addAttribute("rooms", rooms);

		if (capacity != null) {
			model.addAttribute("capacityFilter", capacity);
		}
		if (search != null) {
			model.addAttribute("searchFilter", search);
		}
		model.addAttribute("sortFilter", sort);
	}

	@Override
	public String processRoomCreation(Room room, BindingResult result, RedirectAttributes redirectAttributes,
			MessageSource messageSource) {
		try {
			Room savedRoom = createRoom(room);
			String message = messageSource.getMessage("room.added",
					new Object[] { savedRoom.getName(), savedRoom.getCapacity() }, LocaleContextHolder.getLocale());
			redirectAttributes.addFlashAttribute("message", message);
			return "redirect:/rooms";
		} catch (Exception e) {
			result.rejectValue("name", "error.room", e.getMessage());
			return "rooms/form";
		}
	}

	@Override
	public String processRoomDeletion(Long id, Integer capacity, String search, String sort,
			RedirectAttributes redirectAttributes, MessageSource messageSource) {
		try {
			Room room = getRoomById(id).orElseThrow(() -> new IllegalArgumentException("Invalid room id: " + id));

			if (!room.getEvents().isEmpty()) {
				String errorMsg = messageSource.getMessage("room.delete.events",
						new Object[] { room.getEvents().size() }, LocaleContextHolder.getLocale());
				redirectAttributes.addFlashAttribute("error", errorMsg);
				return buildRedirectUrlWithParams("redirect:/rooms", capacity, search, sort);
			}

			deleteRoom(id);

			String message = messageSource.getMessage("room.deleted", new Object[] { room.getName() },
					LocaleContextHolder.getLocale());
			redirectAttributes.addFlashAttribute("message", message);
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Error deleting room: " + e.getMessage());
		}

		return buildRedirectUrlWithParams("redirect:/rooms", capacity, search, sort);
	}

	@Override
	public String prepareDeleteConfirmation(Long id, Model model, Integer capacity, String search, String sort) {
		Room room = getRoomById(id).orElseThrow(() -> new IllegalArgumentException("Invalid room id: " + id));

		model.addAttribute("room", room);

		if (capacity != null) {
			model.addAttribute("capacityFilter", capacity);
		}
		if (search != null) {
			model.addAttribute("searchFilter", search);
		}
		model.addAttribute("sortFilter", sort);

		return "rooms/confirm-delete";
	}

	@Override
	public String buildRedirectUrlWithParams(String baseUrl, Integer capacity, String search, String sort) {
		StringBuilder redirectUrl = new StringBuilder(baseUrl);
		boolean hasParams = false;

		if (capacity != null) {
			redirectUrl.append(hasParams ? "&capacity=" : "?capacity=").append(capacity);
			hasParams = true;
		}

		if (search != null && !search.isEmpty()) {
			redirectUrl.append(hasParams ? "&search=" : "?search=").append(search);
			hasParams = true;
		}

		if (sort != null) {
			redirectUrl.append(hasParams ? "&sort=" : "?sort=").append(sort);
		}

		return redirectUrl.toString();
	}
}