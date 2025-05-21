package com.springboot.EWDJ_IT_conferentie;

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
        
        List<Room> rooms = roomService.filterRooms(capacity, search, sort);
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
    
    @GetMapping("/{id}")
    public String viewRoom(@PathVariable Long id, Model model) {
        Optional<Room> roomOpt = roomService.getRoomById(id);
        if (roomOpt.isEmpty()) {
            return "redirect:/rooms";
        }
        
        model.addAttribute("room", roomOpt.get());
        return "rooms/detail";
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
        
        Optional<Room> roomOpt = roomService.getRoomById(id);
        if (roomOpt.isEmpty()) {
            return buildRedirectUrl("redirect:/rooms", capacity, search, sort);
        }
        
        Room room = roomOpt.get();
        model.addAttribute("room", room);
        
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
    
    // Helper method
    private String buildRedirectUrl(String baseUrl, Integer capacity, String search, String sort) {
        // Ensure baseUrl doesn't end with a slash to prevent double slashes
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        
        StringBuilder redirectUrl = new StringBuilder(baseUrl);
        boolean hasParams = false;
        
        if (capacity != null) {
            redirectUrl.append(hasParams ? "&capacity=" : "?capacity=").append(capacity);
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