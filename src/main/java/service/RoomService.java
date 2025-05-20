package service;

import java.util.List;
import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import domain.Event;
import domain.Room;

public interface RoomService {

	List<Room> getAllRooms();

	Optional<Room> getRoomById(Long id);

	Optional<Room> getRoomByName(String name);

	Room createRoom(Room room);

	void deleteRoom(Long id);

	List<Event> getRoomEvents(Long roomId);

	boolean existsByName(String name);

	int getRoomCapacity(Long roomId);

	void prepareRoomListModel(Model model, Integer capacity, String search, String sort);

	String processRoomCreation(Room room, BindingResult result, RedirectAttributes redirectAttributes,
			MessageSource messageSource);

	String processRoomDeletion(Long id, Integer capacity, String search, String sort,
			RedirectAttributes redirectAttributes, MessageSource messageSource);

	String prepareDeleteConfirmation(Long id, Model model, Integer capacity, String search, String sort);

	String buildRedirectUrlWithParams(String baseUrl, Integer capacity, String search, String sort);
}