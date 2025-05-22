package com.springboot.EWDJ_IT_conferentie;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import service.AdminService;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/events")
    public String manageEvents(Model model, HttpServletRequest request, HttpSession session,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                               @RequestParam(required = false) Long room, @RequestParam(required = false) Double priceMax,
                               @RequestParam(required = false) String search,
                               @RequestParam(required = false, defaultValue = "datetime") String sort) {


        adminService.storeAdminEventsUrl(request, session);


        adminService.prepareAdminEventsModel(model, dateFrom, dateTo, room, priceMax, search, sort);

        return "admin/events";
    }
}