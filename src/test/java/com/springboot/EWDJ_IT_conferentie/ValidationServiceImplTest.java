package com.springboot.EWDJ_IT_conferentie;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import domain.Event;
import domain.Room;
import repository.EventRepository;
import service.ValidationServiceImpl;

import java.util.stream.Stream;

public class ValidationServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private ValidationServiceImpl validationService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @ParameterizedTest
    @MethodSource("roomAvailabilityTestCases")
    public void testIsRoomAvailable(boolean repositoryReturnValue, boolean expectedResult) {
        LocalDateTime dateTime = LocalDateTime.now();
        Long roomId = 1L;
        
        when(eventRepository.existsByRoomIdAndDateTimeEquals(roomId, dateTime)).thenReturn(repositoryReturnValue);

        boolean result = validationService.isRoomAvailable(dateTime, roomId);
        
        assertEquals(expectedResult, result);
        verify(eventRepository).existsByRoomIdAndDateTimeEquals(roomId, dateTime);
    }
    
    private static Stream<Arguments> roomAvailabilityTestCases() {
        return Stream.of(
            Arguments.of(false, true),
            Arguments.of(true, false)
        );
    }

    @ParameterizedTest
    @MethodSource("eventNameUniqueTestCases")
    public void testIsEventNameUniqueOnDate(List<Event> existingEvents, String testEventName, LocalDateTime testDateTime, boolean expectedResult) {
        when(eventRepository.findAll()).thenReturn(existingEvents);

        boolean result = validationService.isEventNameUniqueOnDate(testDateTime, testEventName);
        
        assertEquals(expectedResult, result);
        verify(eventRepository).findAll();
    }
    
    private static Stream<Arguments> eventNameUniqueTestCases() {
        List<Event> eventsWithDifferentNameSameDate = new ArrayList<>();
        Event event1 = new Event();
        event1.setName("Other Event");
        event1.setDateTime(LocalDateTime.of(2023, 6, 1, 14, 0));
        eventsWithDifferentNameSameDate.add(event1);
        
        List<Event> eventsWithSameNameDifferentDate = new ArrayList<>();
        Event event2 = new Event();
        event2.setName("Test Event");
        event2.setDateTime(LocalDateTime.of(2023, 6, 2, 10, 0));
        eventsWithSameNameDifferentDate.add(event2);
        
        List<Event> eventsWithSameNameSameDate = new ArrayList<>();
        Event event3 = new Event();
        event3.setName("Test Event");
        event3.setDateTime(LocalDateTime.of(2023, 6, 1, 14, 0));
        eventsWithSameNameSameDate.add(event3);
        
        return Stream.of(
            Arguments.of(eventsWithDifferentNameSameDate, "Test Event", LocalDateTime.of(2023, 6, 1, 10, 0), true),
            Arguments.of(eventsWithSameNameDifferentDate, "Test Event", LocalDateTime.of(2023, 6, 1, 10, 0), true),
            Arguments.of(eventsWithSameNameSameDate, "Test Event", LocalDateTime.of(2023, 6, 1, 10, 0), false)
        );
    }

    @ParameterizedTest
    @MethodSource("roomAvailabilityExcludingEventTestCases")
    public void testIsRoomAvailableExcludingEvent(List<Event> existingEvents, boolean expectedResult) {
        LocalDateTime dateTime = LocalDateTime.of(2023, 6, 1, 10, 0);
        Long roomId = 1L;
        Long eventId = 5L;
        
        when(eventRepository.findAll()).thenReturn(existingEvents);

        boolean result = validationService.isRoomAvailableExcludingEvent(dateTime, roomId, eventId);
        
        assertEquals(expectedResult, result);
        verify(eventRepository).findAll();
    }
    
    private static Stream<Arguments> roomAvailabilityExcludingEventTestCases() {
        Room room = new Room();
        room.setId(1L);
        
        List<Event> eventsWithConflict = new ArrayList<>();
        Event conflictEvent = new Event();
        conflictEvent.setId(10L);
        conflictEvent.setDateTime(LocalDateTime.of(2023, 6, 1, 10, 0));
        conflictEvent.setRoom(room);
        eventsWithConflict.add(conflictEvent);
        Event excludedEvent = new Event();
        excludedEvent.setId(5L);
        excludedEvent.setDateTime(LocalDateTime.of(2023, 6, 1, 10, 0));
        excludedEvent.setRoom(room);
        eventsWithConflict.add(excludedEvent);
        
        List<Event> eventsWithoutConflict = new ArrayList<>();
        Event nonConflictEvent = new Event();
        nonConflictEvent.setId(10L);
        nonConflictEvent.setDateTime(LocalDateTime.of(2023, 6, 1, 11, 0));
        nonConflictEvent.setRoom(room);
        eventsWithoutConflict.add(nonConflictEvent);
        eventsWithoutConflict.add(excludedEvent);
        
        return Stream.of(
            Arguments.of(eventsWithConflict, false),
            Arguments.of(eventsWithoutConflict, true)
        );
    }
}