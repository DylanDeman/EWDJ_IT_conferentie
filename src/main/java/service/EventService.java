package service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.validation.BindingResult;

import domain.Event;
import domain.Room;
import domain.Speaker;

public interface EventService {
	List<Event> findAll();

	Optional<Event> findById(Long id);

	Event save(Event event);

	void deleteById(Long id);

	List<Event> findByDate(LocalDateTime date);

	Optional<Integer> getRoomCapacity(Long roomId);

	List<Event> getUserFavorites(String username);

	boolean hasReachedFavoriteLimit(String username);

	void addToFavorites(Long eventId, String username);

	void removeFromFavorites(Long eventId, String username);

	List<Event> getAllEvents();

	Optional<Event> getEventById(Long id);

	List<Event> getEventsByDate(LocalDateTime date);

	Event createEvent(Event event);

	Event updateEvent(Long id, Event event);

	void deleteEvent(Long id);

	boolean isRoomAvailable(LocalDateTime dateTime, Long roomId);

	boolean isEventNameUniqueOnDate(LocalDateTime date, String name);

	List<Event> getFilteredEvents(String dateStr, Long roomId, String sortBy);

	void validateEvent(Event event, BindingResult result);

	Event setupAddEventWithDefaults(Long eventId, Event event);

	List<Room> setupRooms();

	List<Speaker> setupSpeakers();
}