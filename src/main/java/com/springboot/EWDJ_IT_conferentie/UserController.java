package com.springboot.EWDJ_IT_conferentie;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import service.EventService;
import service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {

    private final EventService eventService;
    private final UserService userService;

    public UserController(EventService eventService, UserService userService) {
        this.eventService = eventService;
        this.userService = userService;
    }

    @GetMapping("/favorites")
    public String listFavorites(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("events", eventService.getUserFavorites(userDetails.getUsername()));
        return "user/favorites";
    }
} 