package com.springboot.EWDJ_IT_conferentie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private MessageSource messageSource;

    @GetMapping
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout, Model model) {

        if (error != null) {
            String errorMessage = messageSource.getMessage("login.error", null, LocaleContextHolder.getLocale());
            model.addAttribute("error", errorMessage);
        }
        if (logout != null) {
            String logoutMessage = messageSource.getMessage("login.logout", null, LocaleContextHolder.getLocale());
            model.addAttribute("message", logoutMessage);
        }
        return "login";
    }

}