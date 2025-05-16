package com.springboot.controller.api;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.springboot.service.EventService;

import domain.Event;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/events")
public class EventApiController {

	private final EventService eventService;

	public EventApiController(EventService eventService) {
		this.eventService = eventService;
	}

	@GetMapping
	public ResponseEntity<List<Event>> getAllEvents() {
		return ResponseEntity.ok(eventService.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Event> getEvent(@PathVariable Long id) {
		return eventService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<Event> createEvent(@Valid @RequestBody Event event) {
		Event savedEvent = eventService.save(event);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(savedEvent.getId())
				.toUri();
		return ResponseEntity.created(location).body(savedEvent);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Event> updateEvent(@PathVariable Long id, @Valid @RequestBody Event event) {
		return eventService.findById(id).map(existingEvent -> {
			event.setId(id);
			return ResponseEntity.ok(eventService.save(event));
		}).orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
		return eventService.findById(id).map(event -> {
			eventService.deleteById(id);
			return ResponseEntity.noContent().<Void>build();
		}).orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/date/{date}")
	public ResponseEntity<List<Event>> getEventsByDate(@PathVariable String date) {
		try {
			LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
			LocalDateTime dateTime = LocalDateTime.of(localDate, LocalTime.MIDNIGHT);
			return ResponseEntity.ok(eventService.findByDate(dateTime));
		} catch (DateTimeParseException e) {
			return ResponseEntity.badRequest().build();
		}
	}

	@GetMapping("/room/{roomId}/capacity")
	public ResponseEntity<Integer> getRoomCapacity(@PathVariable Long roomId) {
		return eventService.getRoomCapacity(roomId).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}
}