package com.springboot.EWDJ_IT_conferentie;

import domain.Event;
import domain.MyUser;
import domain.Room;
import domain.Speaker;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.EventService;
import service.RoomService;
import service.SpeakerService;
import service.UserService;
import validation.EventValidator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private SpeakerService speakerService;

    @Autowired
    private EventValidator eventValidator;

    @Autowired
    private UserService userService;

    @GetMapping({"", "/"})
    public String listEvents(Model model,
                             @RequestParam(required = false) String date,
                             @RequestParam(required = false) Long room,
                             @RequestParam(required = false, defaultValue = "datetime") String sort,
                             @AuthenticationPrincipal UserDetails userDetails) {

        List<Event> events = findFilteredEvents(date, room, sort);
        model.addAttribute("events", events);
        model.addAttribute("rooms", roomService.getAllRooms());

        if (userDetails != null) {
            String username = userDetails.getUsername();
            model.addAttribute("isAdmin", userService.isAdmin(username));

            MyUser user = userService.findByUsername(username);
            model.addAttribute("userFavorites", user.getFavorites());
        }

        return "events/list";
    }

    private List<Event> findFilteredEvents(String dateStr, Long roomId, String sortBy) {
        List<Event> events = eventService.findAll();

        if (dateStr != null && !dateStr.isEmpty()) {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
            events = events.stream()
                    .filter(e -> e.getDateTime() != null &&
                            e.getDateTime().toLocalDate().equals(date))
                    .collect(Collectors.toList());
        }

        if (roomId != null) {
            events = events.stream()
                    .filter(e -> e.getRoom() != null &&
                            e.getRoom().getId() == roomId)
                    .collect(Collectors.toList());
        }

        if ("datetime".equalsIgnoreCase(sortBy)) {
            events.sort(Comparator.comparing(Event::getDateTime,
                    Comparator.nullsLast(Comparator.naturalOrder())));
        } else if ("name".equalsIgnoreCase(sortBy)) {
            events.sort(Comparator.comparing(Event::getName,
                    Comparator.nullsFirst(Comparator.naturalOrder())));
        } else if ("room".equalsIgnoreCase(sortBy)) {
            events.sort(Comparator.comparing(
                    e -> e.getRoom() != null ? e.getRoom().getName() : "",
                    Comparator.nullsLast(Comparator.naturalOrder())));
        }

        return events;
    }

    @GetMapping("/{id}")
    public String viewEvent(@PathVariable Long id, Model model, HttpSession session,
                            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Optional<Event> eventOpt = eventService.findById(id);
            if (eventOpt.isEmpty()) {
                throw new IllegalArgumentException("Invalid event id: " + id);
            }

            model.addAttribute("event", eventOpt.get());

            if (userDetails != null && userService.isAdmin(userDetails.getUsername())) {
                String returnUrl = (String) session.getAttribute("adminEventsUrl");
                if (returnUrl != null) {
                    model.addAttribute("returnUrl", returnUrl);
                }
            }

            if (userDetails != null) {
                String username = userDetails.getUsername();
                MyUser user = userService.findByUsername(username);

                boolean isFavorite = user.getFavorites().stream()
                        .anyMatch(event -> event.getId().equals(id));
                model.addAttribute("isFavorite", isFavorite);

                boolean canAddMore = user.getFavorites().size() < 5;
                model.addAttribute("canAddToFavorites", canAddMore);
            } else {
                model.addAttribute("isFavorite", false);
            }

            return "events/detail";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while retrieving the event details");
            return "error";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String showAddEventForm(Model model) {
        Event newEvent = new Event();
        prepareModelForForm(model, newEvent, null, null, null, "0");
        return "events/form";
    }

    @PostMapping("/new")
    public String addEvent(@Valid @ModelAttribute Event event, BindingResult result,
                           @RequestParam(required = false) Long speaker1Id,
                           @RequestParam(required = false) Long speaker2Id,
                           @RequestParam(required = false) Long speaker3Id,
                           @RequestParam Long roomId,
                           @RequestParam(required = false) String beamerCheck,
                           @RequestParam(required = false) String action,
                           Model model, RedirectAttributes redirectAttributes) {

        setupEvent(event, roomId, speaker1Id, speaker2Id, speaker3Id);

        if ("calculate".equals(action)) {
            int calculatedCheck = calculateBeamerCheck(event.getBeamerCode());
            prepareModelForForm(model, event, speaker1Id, speaker2Id, speaker3Id, String.valueOf(calculatedCheck));
            return "events/form";
        }

        if ("addSpeaker".equals(action)) {
            prepareModelForForm(model, event, speaker1Id, speaker2Id, speaker3Id, beamerCheck);
            model.addAttribute("showSpeakerSelection", true);
            return "events/form";
        }

        if (event.getSpeakers() == null || event.getSpeakers().isEmpty()) {
            model.addAttribute("speakerError", "At least one speaker is required");
            prepareModelForForm(model, event, speaker1Id, speaker2Id, speaker3Id, beamerCheck);
            return "events/form";
        }

        try {
            if (beamerCheck != null && !beamerCheck.isEmpty()) {
                event.setBeamerCheck(Integer.parseInt(beamerCheck));
            } else {
                event.setBeamerCheck(calculateBeamerCheck(event.getBeamerCode()));
            }
        } catch (NumberFormatException e) {
            result.rejectValue("beamerCheck", "error.beamerCheck.format", "Beamer check must be a number");
            model.addAttribute("beamerCheckError", "Beamer check must be a number");
        }

        eventValidator.validate(event, result);

        if (result.hasErrors()) {
            prepareModelForForm(model, event, speaker1Id, speaker2Id, speaker3Id,
                    beamerCheck != null ? beamerCheck : String.valueOf(calculateBeamerCheck(event.getBeamerCode())));
            return "events/form";
        }

        try {
            eventService.save(event);
            redirectAttributes.addFlashAttribute("message", "Event created successfully");
            return "redirect:/admin/events";
        } catch (Exception e) {
            result.rejectValue(null, null, e.getMessage());
            prepareModelForForm(model, event, speaker1Id, speaker2Id, speaker3Id,
                    beamerCheck != null ? beamerCheck : String.valueOf(calculateBeamerCheck(event.getBeamerCode())));
            return "events/form";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/edit")
    public String showEditEventForm(@PathVariable Long id, Model model) {
        Optional<Event> eventOpt = eventService.findById(id);
        if (eventOpt.isEmpty()) {
            return "redirect:/events";
        }

        Event event = eventOpt.get();
        prepareModelForForm(model, event, null, null, null, String.valueOf(event.getBeamerCheck()));
        return "events/form";
    }

    @PutMapping("/{id}/edit")
    public String editEvent(@PathVariable Long id, @ModelAttribute Event event, BindingResult result,
                            @RequestParam Long roomId,
                            @RequestParam(required = false) Long speaker1Id,
                            @RequestParam(required = false) Long speaker2Id,
                            @RequestParam(required = false) Long speaker3Id,
                            @RequestParam(required = false) String beamerCheck,
                            @RequestParam(required = false) String action,
                            Model model,
                            RedirectAttributes redirectAttributes) {

        setupEvent(event, roomId, speaker1Id, speaker2Id, speaker3Id);

        if ("calculate".equals(action)) {
            int calculatedCheck = calculateBeamerCheck(event.getBeamerCode());
            prepareModelForForm(model, event, speaker1Id, speaker2Id, speaker3Id, String.valueOf(calculatedCheck));
            return "events/form";
        }

        if ("addSpeaker".equals(action)) {
            prepareModelForForm(model, event, speaker1Id, speaker2Id, speaker3Id, beamerCheck);
            model.addAttribute("showSpeakerSelection", true);
            return "events/form";
        }

        if (event.getSpeakers() == null || event.getSpeakers().isEmpty()) {
            model.addAttribute("speakerError", "At least one speaker is required");
            prepareModelForForm(model, event, speaker1Id, speaker2Id, speaker3Id, beamerCheck);
            return "events/form";
        }

        try {
            if (beamerCheck != null && !beamerCheck.isEmpty()) {
                event.setBeamerCheck(Integer.parseInt(beamerCheck));
            } else {
                event.setBeamerCheck(calculateBeamerCheck(event.getBeamerCode()));
            }
        } catch (NumberFormatException e) {
            result.rejectValue("beamerCheck", "error.beamerCheck.format", "Beamer check must be a number");
            model.addAttribute("beamerCheckError", "Beamer check must be a number");
        }

        eventValidator.validate(event, result);

        if (result.hasErrors()) {
            prepareModelForForm(model, event, speaker1Id, speaker2Id, speaker3Id,
                    beamerCheck != null ? beamerCheck : String.valueOf(calculateBeamerCheck(event.getBeamerCode())));
            return "events/form";
        }

        try {
            eventService.save(event);
            redirectAttributes.addFlashAttribute("message", "Event updated successfully");
            return "redirect:/admin/events";
        } catch (Exception e) {
            result.rejectValue("", "", e.getMessage());
            prepareModelForForm(model, event, speaker1Id, speaker2Id, speaker3Id,
                    beamerCheck != null ? beamerCheck : String.valueOf(calculateBeamerCheck(event.getBeamerCode())));
            return "events/form";
        }
    }

    private void setupEvent(Event event, Long roomId, Long speaker1Id, Long speaker2Id, Long speaker3Id) {
        if (roomId != null) {
            Optional<Room> roomOpt = roomService.getRoomById(roomId);
            if (roomOpt.isPresent()) {
                event.setRoom(roomOpt.get());
            }
        }

        List<Speaker> speakers = new ArrayList<>();

        if (speaker1Id != null && speaker1Id > 0) {
            Speaker speaker1 = speakerService.findById(speaker1Id);
            if (speaker1 != null) {
                speakers.add(speaker1);
            }
        }

        if (speaker2Id != null && speaker2Id > 0) {
            Speaker speaker2 = speakerService.findById(speaker2Id);
            if (speaker2 != null) {
                speakers.add(speaker2);
            }
        }

        if (speaker3Id != null && speaker3Id > 0) {
            Speaker speaker3 = speakerService.findById(speaker3Id);
            if (speaker3 != null) {
                speakers.add(speaker3);
            }
        }

        event.setSpeakers(speakers);
    }

    private void prepareModelForForm(Model model, Event event, Long speaker1Id, Long speaker2Id, Long speaker3Id, String beamerCheck) {
        model.addAttribute("event", event);
        model.addAttribute("rooms", roomService.getAllRooms());

        List<Speaker> speakers = speakerService.findAll();
        model.addAttribute("allSpeakers", speakers);
        model.addAttribute("speakers", speakers);

        model.addAttribute("beamerCheck", beamerCheck);
        model.addAttribute("speaker1Id", speaker1Id);
        model.addAttribute("speaker2Id", speaker2Id);
        model.addAttribute("speaker3Id", speaker3Id);

        List<Speaker> speakerList = event.getSpeakers();
        if (speakerList != null && !speakerList.isEmpty()) {
            if (speakerList.size() >= 1) {
                model.addAttribute("speaker1", speakerList.get(0));
                if (speaker1Id == null) {
                    model.addAttribute("speaker1Id", speakerList.get(0).getId());
                }
            }
            if (speakerList.size() >= 2) {
                model.addAttribute("speaker2", speakerList.get(1));
                if (speaker2Id == null) {
                    model.addAttribute("speaker2Id", speakerList.get(1).getId());
                }
            }
            if (speakerList.size() >= 3) {
                model.addAttribute("speaker3", speakerList.get(2));
                if (speaker3Id == null) {
                    model.addAttribute("speaker3Id", speakerList.get(2).getId());
                }
            }
        }

        if (event.getRoom() != null) {
            model.addAttribute("selectedRoomId", event.getRoom().getId());
        }
    }

    private int calculateBeamerCheck(int beamerCode) {
        return beamerCode % 97;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/delete")
    public String deleteEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            eventService.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Event deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting event: " + e.getMessage());
        }
        return "redirect:/admin/events";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/confirm")
    public String confirmDeleteEvent(@PathVariable Long id, Model model, HttpSession session) {
        String referer = session.getAttribute("adminEventsUrl") != null ?
                (String) session.getAttribute("adminEventsUrl") :
                "/admin/events";

        Optional<Event> eventOptional = eventService.findById(id);

        if (eventOptional.isPresent()) {
            model.addAttribute("event", eventOptional.get());
            model.addAttribute("returnUrl", referer);
            return "events/confirm-delete";
        } else {
            return "redirect:" + referer;
        }
    }
}