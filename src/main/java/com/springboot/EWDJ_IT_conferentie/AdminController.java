package com.springboot.EWDJ_IT_conferentie;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import domain.Event;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import service.EventService;
import service.RoomService;
import service.UserService;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

	@Autowired
	private EventService eventService;

	@Autowired
	private RoomService roomService;

	@Autowired
	private UserService userService;

	@GetMapping("/events")
	public String manageEvents(Model model, HttpServletRequest request, HttpSession session,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
			@RequestParam(required = false) Long room, @RequestParam(required = false) Double priceMax,
			@RequestParam(required = false) String search,
			@RequestParam(required = false, defaultValue = "datetime") String sort) {

		session.setAttribute("adminEventsUrl", request.getRequestURL().toString()
				+ (request.getQueryString() != null ? "?" + request.getQueryString() : ""));

		List<Event> events = eventService.getAllEvents();

		if (dateFrom != null) {
			LocalDateTime fromDateTime = dateFrom.atStartOfDay();
			events = events.stream()
					.filter(e -> e.getDateTime().isEqual(fromDateTime) || e.getDateTime().isAfter(fromDateTime))
					.collect(Collectors.toList());
		}

		if (dateTo != null) {
			LocalDateTime toDateTime = dateTo.plusDays(1).atStartOfDay();
			events = events.stream().filter(e -> e.getDateTime().isBefore(toDateTime)).collect(Collectors.toList());
		}

		if (room != null) {
			events = events.stream().filter(e -> e.getRoom().getId() == room).collect(Collectors.toList());
		}

		if (priceMax != null) {
			BigDecimal maxPrice = BigDecimal.valueOf(priceMax);
			events = events.stream().filter(e -> e.getPrice().compareTo(maxPrice) <= 0).collect(Collectors.toList());
		}

		if (search != null && !search.trim().isEmpty()) {
			String searchLower = search.toLowerCase();
			events = events.stream()
					.filter(e -> e.getName().toLowerCase().contains(searchLower)
							|| e.getDescription().toLowerCase().contains(searchLower)
							|| e.getSpeakers().stream().anyMatch(s -> s.getName().toLowerCase().contains(searchLower)))
					.collect(Collectors.toList());
		}

		switch (sort) {
		case "name":
			events.sort((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));
			break;
		case "name_desc":
			events.sort((e1, e2) -> e2.getName().compareToIgnoreCase(e1.getName()));
			break;
		case "price":
			events.sort((e1, e2) -> e1.getPrice().compareTo(e2.getPrice()));
			break;
		case "price_desc":
			events.sort((e1, e2) -> e2.getPrice().compareTo(e1.getPrice()));
			break;
		case "datetime_desc":
			events.sort((e1, e2) -> e2.getDateTime().compareTo(e1.getDateTime()));
			break;
		case "popularity":

			Map<Long, Integer> popularityMap = new HashMap<>();
			events.forEach(e -> {
				int count = (int) userService.findAll().stream()
						.filter(u -> u.getFavorites().stream().anyMatch(fe -> fe.getId().equals(e.getId()))).count();
				popularityMap.put(e.getId(), count);
			});
			events.sort((e1, e2) -> Integer.compare(popularityMap.getOrDefault(e2.getId(), 0),
					popularityMap.getOrDefault(e1.getId(), 0)));
			break;
		default:
			events.sort((e1, e2) -> e1.getDateTime().compareTo(e2.getDateTime()));
		}

		List<Event> allEvents = eventService.getAllEvents();
		int totalEvents = allEvents.size();

		LocalDateTime now = LocalDateTime.now();
		int upcomingEvents = (int) allEvents.stream().filter(e -> e.getDateTime().isAfter(now)).count();

		Map<Event, Long> eventPopularity = new HashMap<>();
		for (Event event : allEvents) {
			long favoriteCount = userService.findAll().stream()
					.filter(u -> u.getFavorites().stream().anyMatch(fe -> fe.getId().equals(event.getId()))).count();
			eventPopularity.put(event, favoriteCount);
		}

		Event mostFavoritedEvent = eventPopularity.entrySet().stream().max(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey).orElse(null);

		BigDecimal avgPrice = allEvents.stream().map(Event::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add)
				.divide(BigDecimal.valueOf(allEvents.isEmpty() ? 1 : allEvents.size()), 2, RoundingMode.HALF_UP);

		Map<Long, Integer> eventFavorites = new HashMap<>();
		for (Event event : events) {
			int count = (int) userService.findAll().stream()
					.filter(u -> u.getFavorites().stream().anyMatch(fe -> fe.getId().equals(event.getId()))).count();
			if (count > 0) {
				eventFavorites.put(event.getId(), count);
			}
		}

		model.addAttribute("events", events);
		model.addAttribute("rooms", roomService.getAllRooms());
		model.addAttribute("totalEvents", totalEvents);
		model.addAttribute("upcomingEvents", upcomingEvents);
		model.addAttribute("mostFavoritedEvent", mostFavoritedEvent);
		model.addAttribute("avgPrice", avgPrice);
		model.addAttribute("eventFavorites", eventFavorites);

		model.addAttribute("dateFrom", dateFrom);
		model.addAttribute("dateTo", dateTo);
		model.addAttribute("room", room);
		model.addAttribute("priceMax", priceMax);
		model.addAttribute("search", search);
		model.addAttribute("sort", sort);

		return "admin/events";
	}
}