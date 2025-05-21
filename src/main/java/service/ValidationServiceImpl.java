package service;

import domain.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.EventRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ValidationServiceImpl implements ValidationService {

	@Autowired
	private EventRepository eventRepository;

	@Override
	public boolean isRoomAvailable(LocalDateTime dateTime, Long roomId) {
		return !eventRepository.existsByRoomIdAndDateTimeEquals(roomId, dateTime);
	}

	@Override
	public boolean isEventNameUniqueOnDate(LocalDateTime date, String name) {

        return eventRepository.findAll().stream()
                .noneMatch(e -> e.getName().equalsIgnoreCase(name) &&
                         e.getDateTime() != null && 
                         e.getDateTime().toLocalDate().equals(date.toLocalDate()));
	}

	@Override
	public boolean existsByNameAndDate(String name, LocalDate date) {
		return eventRepository.findAll().stream()
				.anyMatch(e -> e.getName().equals(name) && e.getDateTime().toLocalDate().equals(date));
	}

	@Override
	public boolean existsByNameAndDateExcludingId(String name, LocalDate date, Long eventId) {
		List<Event> eventsOnDate = eventRepository.findAll().stream()
				.filter(e -> e.getDateTime() != null && e.getDateTime().toLocalDate().equals(date))
				.toList();

		return eventsOnDate.stream().filter(e -> !e.getId().equals(eventId))
				.anyMatch(e -> e.getName().equalsIgnoreCase(name));
	}

	@Override
	public boolean isRoomAvailableExcludingEvent(LocalDateTime dateTime, Long roomId, Long eventId) {
		List<Event> eventsAtDateTime = eventRepository.findAll().stream().filter(
				e -> e.getDateTime().equals(dateTime) && e.getRoom().getId() == roomId && !e.getId().equals(eventId))
				.toList();
		return eventsAtDateTime.isEmpty();
	}
}