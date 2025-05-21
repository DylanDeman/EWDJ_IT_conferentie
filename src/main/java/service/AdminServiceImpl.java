package service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import domain.Event;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Service
public class AdminServiceImpl implements AdminService {

	@Autowired
	private EventService eventService;

	@Autowired
	private RoomService roomService;

	@Autowired
	private UserService userService;

	@Override
	public void storeAdminEventsUrl(HttpServletRequest request, HttpSession session) {
		String url = request.getRequestURL().toString();
		String queryString = request.getQueryString();

		session.setAttribute("adminEventsUrl", url + (queryString != null ? "?" + queryString : ""));
	}

	@Override
	public void prepareAdminEventsModel(Model model, LocalDate dateFrom, LocalDate dateTo, Long room, Double priceMax,
			String search, String sort) {

		List<Event> filteredEvents = getFilteredAndSortedEvents(dateFrom, dateTo, room, priceMax, search, sort);

		Map<Long, Integer> eventFavorites = calculateEventFavorites(filteredEvents);

		model.addAttribute("events", filteredEvents);
		model.addAttribute("rooms", roomService.getAllRooms());
		model.addAttribute("eventFavorites", eventFavorites);

		model.addAttribute("dateFrom", dateFrom);
		model.addAttribute("dateTo", dateTo);
		model.addAttribute("room", room);
		model.addAttribute("priceMax", priceMax);
		model.addAttribute("search", search);
		model.addAttribute("sort", sort);
	}

	private List<Event> getFilteredAndSortedEvents(LocalDate dateFrom, LocalDate dateTo, Long room, Double priceMax,
			String search, String sort) {

		List<Event> events = eventService.findAll();

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

		sortEventsList(events, sort);

		return events;
	}

	private void sortEventsList(List<Event> events, String sort) {
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
	}

	private Map<Long, Integer> calculateEventFavorites(List<Event> events) {
		Map<Long, Integer> eventFavorites = new HashMap<>();

		for (Event event : events) {
			int count = (int) userService.findAll().stream()
					.filter(u -> u.getFavorites().stream().anyMatch(fe -> fe.getId().equals(event.getId()))).count();
			if (count > 0) {
				eventFavorites.put(event.getId(), count);
			}
		}

		return eventFavorites;
	}
}