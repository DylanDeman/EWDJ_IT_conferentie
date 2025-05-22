package com.springboot.EWDJ_IT_conferentie;

import DTO.RoomWithEventCount;
import domain.Room;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.RoomService;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping
    public String listRooms(Model model,
                            @RequestParam(required = false) Integer capacity,
                            @RequestParam(required = false) String search,
                            @RequestParam(required = false, defaultValue = "name") String sort) {


        List<RoomWithEventCount> rooms = roomService.filterRoomsWithEventCount(capacity, search, sort);
        model.addAttribute("rooms", rooms);

        if (capacity != null) {
            model.addAttribute("capacityFilter", capacity);
        }
        if (search != null) {
            model.addAttribute("searchFilter", search);
        }
        model.addAttribute("sortFilter", sort);

        return "rooms/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String showAddRoomForm(Model model) {
        model.addAttribute("room", new Room());
        return "rooms/form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/new")
    public String addRoom(@Valid @ModelAttribute Room room,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "rooms/form";
        }
        
        try {
            Room savedRoom = roomService.save(room);
            String message = messageSource.getMessage("room.added",
                    new Object[] { savedRoom.getName(), savedRoom.getCapacity() }, 
                    LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/rooms";
        } catch (Exception e) {
            result.rejectValue("name", "error.room", e.getMessage());
            return "rooms/form";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/confirm")
    public String confirmDeleteRoom(@PathVariable Long id,
                                    Model model,
                                    @RequestParam(required = false) Integer capacity,
                                    @RequestParam(required = false) String search,
                                    @RequestParam(required = false) String sort) {


        List<RoomWithEventCount> rooms = roomService.filterRoomsWithEventCount(null, null, "name");


        Optional<RoomWithEventCount> roomOpt = rooms.stream()
                .filter(room -> room.getId().equals(id))
                .findFirst();

        if (roomOpt.isEmpty()) {
            return buildRedirectUrl("redirect:/rooms", capacity, search, sort);
        }

        RoomWithEventCount room = roomOpt.get();
        model.addAttribute("room", room);


        boolean canDelete = room.getEventCount() == 0;
        model.addAttribute("canDelete", canDelete);

        if (capacity != null) {
            model.addAttribute("capacityFilter", capacity);
        }
        if (search != null) {
            model.addAttribute("searchFilter", search);
        }
        model.addAttribute("sortFilter", sort);

        return "rooms/confirm-delete";
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/delete")
    public String deleteRoom(@PathVariable Long id,
                           @RequestParam(required = false) Integer capacity,
                           @RequestParam(required = false) String search,
                           @RequestParam(required = false) String sort,
                           RedirectAttributes redirectAttributes) {
        
        try {
            Optional<Room> roomOpt = roomService.getRoomById(id);
            if (roomOpt.isEmpty()) {
                throw new IllegalArgumentException("Invalid room id: " + id);
            }
            
            Room room = roomOpt.get();
            
            if (!room.getEvents().isEmpty()) {
                String errorMsg = messageSource.getMessage("room.delete.events",
                        new Object[] { room.getEvents().size() }, 
                        LocaleContextHolder.getLocale());
                redirectAttributes.addFlashAttribute("error", errorMsg);
                return buildRedirectUrl("redirect:/rooms", capacity, search, sort);
            }
            
            roomService.deleteById(id);
            
            String message = messageSource.getMessage("room.deleted", 
                    new Object[] { room.getName() },
                    LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("message", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting room: " + e.getMessage());
        }
        
        return buildRedirectUrl("redirect:/rooms", capacity, search, sort);
    }
    

    private String buildRedirectUrl(String baseUrl, Integer capacity, String search, String sort) {

        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        
        StringBuilder redirectUrl = new StringBuilder(baseUrl);
        boolean hasParams = false;
        
        if (capacity != null) {
            redirectUrl.append("?capacity=").append(capacity);
            hasParams = true;
        }
        
        if (search != null && !search.isEmpty()) {
            redirectUrl.append(hasParams ? "&search=" : "?search=").append(search);
            hasParams = true;
        }
        
        if (sort != null) {
            redirectUrl.append(hasParams ? "&sort=" : "?sort=").append(sort);
        }
        
        return redirectUrl.toString();
    }
}