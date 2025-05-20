package service;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ValidationService {
	boolean isRoomAvailable(LocalDateTime dateTime, Long roomId);

	boolean isEventNameUniqueOnDate(LocalDateTime date, String name);

	boolean existsByNameAndDate(String name, LocalDate date);

	boolean existsByNameAndDateExcludingId(String name, LocalDate date, Long eventId);

	boolean isRoomAvailableExcludingEvent(LocalDateTime dateTime, Long roomId, Long eventId);
}