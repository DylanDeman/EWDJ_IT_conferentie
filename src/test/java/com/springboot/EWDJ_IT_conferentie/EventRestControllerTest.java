package com.springboot.EWDJ_IT_conferentie;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import domain.Event;
import service.EventService;
import service.UserService;

public class EventRestControllerTest {

    private EventService eventService;
    private UserService userService;
    private EventRestController eventRestController;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        eventService = mock(EventService.class);
        userService = mock(UserService.class);
        eventRestController = new EventRestController();

        ReflectionTestUtils.setField(eventRestController, "eventService", eventService);
        ReflectionTestUtils.setField(eventRestController, "userService", userService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(eventRestController)
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void testGetEventsByDate_ISO_Format() throws Exception {
        LocalDate testDate = LocalDate.of(2024, 12, 25);
        Event event1 = createTestEvent(1L, "Christmas Conference", testDate);
        Event event2 = createTestEvent(2L, "Holiday Workshop", testDate);
        List<Event> events = Arrays.asList(event1, event2);

        when(eventService.getEventsByDate(eq(testDate))).thenReturn(events);

        String responseString = mockMvc.perform(get("/api/events/2024-12-25"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();


        System.out.println("Response content: " + responseString);


        verify(eventService).getEventsByDate(testDate);


        assert responseString.contains("id") : "Response should contain 'id'";
        assert responseString.contains("name") : "Response should contain 'name'";
        assert responseString.contains("Christmas Conference") : "Response should contain 'Christmas Conference'";
        assert responseString.contains("Holiday Workshop") : "Response should contain 'Holiday Workshop'";
    }

    @Test
    public void testGetEventsByDate_DashFormat() throws Exception {
        LocalDate testDate = LocalDate.of(2024, 12, 25);
        Event event1 = createTestEvent(1L, "Christmas Conference", testDate);

        when(eventService.getEventsByDate(eq(testDate))).thenReturn(Collections.singletonList(event1));

        String responseString = mockMvc.perform(get("/api/events/25-12-2024"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();


        System.out.println("Response content: " + responseString);


        verify(eventService).getEventsByDate(testDate);

        assert responseString.contains("id") : "Response should contain 'id'";
        assert responseString.contains("name") : "Response should contain 'name'";
        assert responseString.contains("Christmas Conference") : "Response should contain 'Christmas Conference'";
    }

    @ParameterizedTest
    @ValueSource(strings = {"2024/12/25", "25/12/2024", "invalid-date", "25122024"})
    public void testGetEventsByDate_InvalidFormat(String invalidDate) throws Exception {
        try {
            mockMvc.perform(get("/api/events/" + invalidDate));
        } catch (Exception e) {

            Throwable rootCause = getRootCause(e);
            assert rootCause instanceof IllegalArgumentException :
                    "Expected IllegalArgumentException, but got: " + rootCause.getClass().getName();
            assert rootCause.getMessage().contains("Invalid date format") :
                    "Exception message should contain 'Invalid date format'";
        }
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        if (cause == null) {
            return throwable;
        }
        return getRootCause(cause);
    }

    @Test
    public void testGetEventsByDate_NoEvents() throws Exception {
        LocalDate testDate = LocalDate.of(2024, 12, 26);

        when(eventService.getEventsByDate(eq(testDate))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/events/2024-12-26"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(eventService).getEventsByDate(testDate);
    }

    @Test
    public void testGetUserFavorites_Success() throws Exception {
        String username = "testuser";
        Event event1 = createTestEvent(1L, "Java Conference", LocalDate.of(2024, 10, 15));
        Event event2 = createTestEvent(2L, "Spring Workshop", LocalDate.of(2024, 11, 20));
        List<Event> favorites = Arrays.asList(event1, event2);

        when(userService.getSortedUserFavorites(eq(username))).thenReturn(favorites);

        String responseString = mockMvc.perform(get("/api/events/user/" + username + "/favorites"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();


        System.out.println("Response content: " + responseString);


        verify(userService).getSortedUserFavorites(username);

        // Manual verification of JSON response
        assert responseString.contains("id") : "Response should contain 'id'";
        assert responseString.contains("name") : "Response should contain 'name'";
        assert responseString.contains("Java Conference") : "Response should contain 'Java Conference'";
        assert responseString.contains("Spring Workshop") : "Response should contain 'Spring Workshop'";
    }

    @Test
    public void testGetUserFavorites_NoFavorites() throws Exception {
        String username = "newuser";

        when(userService.getSortedUserFavorites(eq(username))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/events/user/" + username + "/favorites"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(userService).getSortedUserFavorites(username);
    }

    @Test
    public void testGetUserFavorites_UserNotFound() throws Exception {
        String username = "nonexistent";

        when(userService.getSortedUserFavorites(eq(username))).thenThrow(new IllegalArgumentException("User not found"));

        try {
            mockMvc.perform(get("/api/events/user/" + username + "/favorites"));
        } catch (Exception e) {
            // Verify the root cause is an IllegalArgumentException with the expected message
            Throwable rootCause = getRootCause(e);
            assert rootCause instanceof IllegalArgumentException :
                    "Expected IllegalArgumentException, but got: " + rootCause.getClass().getName();
            assert rootCause.getMessage().contains("User not found") :
                    "Exception message should contain 'User not found'";
        }

        verify(userService).getSortedUserFavorites(username);
    }

    private Event createTestEvent(Long id, String name, LocalDate date) {
        Event event = new Event();
        event.setId(id);
        event.setName(name);
        event.setDateTime(LocalDateTime.of(date, java.time.LocalTime.of(9, 0)));

        return event;
    }
}