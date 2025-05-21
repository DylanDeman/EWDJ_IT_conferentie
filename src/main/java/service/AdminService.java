package service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;

import java.time.LocalDate;

public interface AdminService {

	void storeAdminEventsUrl(HttpServletRequest request, HttpSession session);

	void prepareAdminEventsModel(Model model, LocalDate dateFrom, LocalDate dateTo, Long room, Double priceMax,
			String search, String sort);
}