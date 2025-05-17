package com.springboot.EWDJ_IT_conferentie;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	@GetMapping("/")
	public String redirectToEvents() {
		return "redirect:/events";
	}
}
