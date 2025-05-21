package com.springboot.EWDJ_IT_conferentie;

import domain.Event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.EventService;

import service.UserService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;


@RestController
@RequestMapping(value = "/api/events")
public class EventRestController {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    private static final DateTimeFormatter DASH_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");


    @GetMapping(value = "/{date}")
    public List<Event> getEventsByDate(@PathVariable String date) {
        LocalDate parsedDate;

        try {
            if (date.contains("-") && date.matches("\\d{2}-\\d{2}-\\d{4}")) {
                parsedDate = LocalDate.parse(date, DASH_DATE_FORMATTER);
            } else {
                parsedDate = LocalDate.parse(date);
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + date);
        }

        return eventService.getEventsByDate(parsedDate);
    }

    @GetMapping(value = "/user/{username}/favorites")
    public List<Event> getUserFavorites(@PathVariable String username) {
        return userService.getSortedUserFavorites(username);
    }
}