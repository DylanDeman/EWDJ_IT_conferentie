package service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import domain.Event;
import domain.Room;
import domain.Speaker;
import jakarta.servlet.http.HttpSession;

public interface EventService {
	// Basic CRUD operations
	List<Event> getAllEvents();

	Optional<Event> getEventById(Long id);

	Event createEvent(Event event);

	Event updateEvent(Long id, Event event);

	void deleteEvent(Long id);

	List<Event> getUserFavorites(String username);

	boolean hasReachedFavoriteLimit(String username);

	void addToFavorites(Long eventId, String username);

	void removeFromFavorites(Long eventId, String username);

	List<Event> getFilteredEvents(String dateStr, Long roomId, String sortBy);

	List<Event> getEventsByDate(LocalDateTime date);

	boolean isRoomAvailable(LocalDateTime dateTime, Long roomId);

	boolean isEventNameUniqueOnDate(LocalDateTime date, String name);

	boolean existsByNameAndDate(String name, LocalDate eventDate);

	boolean existsByNameAndDateExcludingId(String name, LocalDate date, Long eventId);

	void validateEvent(Event event, BindingResult result);

	List<Room> setupRooms();

	List<Speaker> setupSpeakers();

	Optional<Integer> getRoomCapacity(Long roomId);

	Event createEmptyEvent();

	void populateEventFormModel(Model model, Event event);

	void prepareUserEventContext(Model model, String username, Long eventId);

	void setSpeakersForEvent(Event event, Long speaker1Id, Long speaker2Id, Long speaker3Id);

	String handleCalculateAction(Event event, Long roomId, Long speaker1Id, Long speaker2Id, Long speaker3Id,
			Model model);

	String validateAndPrepareEvent(Event event, BindingResult result, Long roomId, Long speaker1Id, Long speaker2Id,
			Long speaker3Id, String beamerCheck, Model model);

	String validateAndPrepareEventForUpdate(Long id, Event event, BindingResult result, Long roomId, Long speaker1Id,
			Long speaker2Id, Long speaker3Id, String beamerCheck, Model model);

	String handleAddSpeakerAction(Event event, Long speaker1Id, Long speaker2Id, Long speaker3Id, String beamerCheck,
			Model model);

	String prepareEditEventForm(Long id, Model model, HttpSession session);

	String prepareConfirmDeleteEvent(Long id, Model model, HttpSession session);
}