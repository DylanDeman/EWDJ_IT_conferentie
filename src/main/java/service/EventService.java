package service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import domain.Event;

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
}