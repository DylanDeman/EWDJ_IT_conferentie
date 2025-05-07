package com.springboot.controller.api;

import com.springboot.domain.Event;
import com.springboot.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventApiController.class)
class EventApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @Test
    @WithMockUser
    void getAllEvents_ShouldReturnEvents() throws Exception {
        Event event = createSampleEvent();
        when(eventService.findAll()).thenReturn(Arrays.asList(event));

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Event"));
    }

    @Test
    @WithMockUser
    void getEvent_WhenExists_ShouldReturnEvent() throws Exception {
        Event event = createSampleEvent();
        when(eventService.findById(1L)).thenReturn(Optional.of(event));

        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Event"));
    }

    @Test
    @WithMockUser
    void getEvent_WhenNotExists_ShouldReturn404() throws Exception {
        when(eventService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEvent_ShouldReturnCreatedEvent() throws Exception {
        Event event = createSampleEvent();
        when(eventService.save(any(Event.class))).thenReturn(event);

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test Event\",\"description\":\"Test Description\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Event"));
    }

    private Event createSampleEvent() {
        Event event = new Event();
        event.setId(1L);
        event.setName("Test Event");
        event.setDescription("Test Description");
        event.setDateTime(LocalDateTime.now());
        event.setPrice(new BigDecimal("19.99"));
        return event;
    }
} 