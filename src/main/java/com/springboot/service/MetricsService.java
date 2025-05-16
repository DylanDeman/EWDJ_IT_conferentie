package com.springboot.service;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.springboot.repository.EventRepository;
import com.springboot.repository.UserRepository;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class MetricsService {

	private final MeterRegistry registry;
	private final Timer eventCreationTimer;
	private final Timer eventUpdateTimer;
	private final Timer eventDeletionTimer;
	private final Timer eventViewTimer;
	private final EventRepository eventRepository;
	private final UserRepository userRepository;

	public MetricsService(MeterRegistry registry, EventRepository eventRepository, UserRepository userRepository) {
		this.registry = registry;
		this.eventRepository = eventRepository;
		this.userRepository = userRepository;
		this.eventCreationTimer = Timer.builder("event.creation.time").description("Time taken to create an event")
				.register(registry);
		this.eventUpdateTimer = Timer.builder("event.update.time").description("Time taken to update an event")
				.register(registry);
		this.eventDeletionTimer = Timer.builder("event.deletion.time").description("Time taken to delete an event")
				.register(registry);
		this.eventViewTimer = Timer.builder("event.view.time").description("Time taken to view an event")
				.register(registry);
	}

	public void incrementEventCreated() {
		registry.counter("app.events.created").increment();
		updateEventsTotal();
	}

	public void incrementEventUpdated() {
		registry.counter("app.events.updated").increment();
	}

	public void incrementEventDeleted() {
		registry.counter("app.events.deleted").increment();
		updateEventsTotal();
	}

	public void incrementFavoriteAdded() {
		registry.counter("app.favorites.added").increment();
		updateFavoritesTotal();
	}

	public void incrementFavoriteRemoved() {
		registry.counter("app.favorites.removed").increment();
		updateFavoritesTotal();
	}

	public void updateEventsTotal(int count) {
		registry.gauge("app.events.total", count);
	}

	public void updateRoomsTotal(int count) {
		registry.gauge("app.rooms.total", count);
	}

	public void updateUsersTotal(int count) {
		registry.gauge("app.users.total", count);
	}

	public void updateFavoritesTotal(int count) {
		registry.gauge("app.favorites.total", count);
	}

	private void updateEventsTotal() {
		long totalEvents = eventRepository.count();
		updateEventsTotal((int) totalEvents);
	}

	private void updateFavoritesTotal() {
		long totalFavorites = userRepository.findAll().stream().mapToLong(user -> user.getFavorites().size()).sum();
		updateFavoritesTotal((int) totalFavorites);
	}

	public void incrementEventViews(Long eventId) {
		registry.counter("event.views", "eventId", eventId.toString()).increment();
	}

	public void incrementFavoriteAdds(Long eventId) {
		registry.counter("event.favorites.adds", "eventId", eventId.toString()).increment();
	}

	public void incrementFavoriteRemoves(Long eventId) {
		registry.counter("event.favorites.removes", "eventId", eventId.toString()).increment();
	}

	public void recordEventCreationTime(double timeInSeconds) {
		eventCreationTimer.record(TimeUnit.SECONDS.toMillis((long) timeInSeconds), TimeUnit.MILLISECONDS);
	}

	public void recordEventUpdateTime(double timeInSeconds) {
		eventUpdateTimer.record(TimeUnit.SECONDS.toMillis((long) timeInSeconds), TimeUnit.MILLISECONDS);
	}

	public void recordEventDeletionTime(double timeInSeconds) {
		eventDeletionTimer.record(TimeUnit.SECONDS.toMillis((long) timeInSeconds), TimeUnit.MILLISECONDS);
	}

	public void recordEventViewTime(double timeInSeconds) {
		eventViewTimer.record(TimeUnit.SECONDS.toMillis((long) timeInSeconds), TimeUnit.MILLISECONDS);
	}
}