package service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Service
public class FavoriteServiceImpl implements FavoriteService {

	@Autowired
	private EventService eventService;

	@Override
	public String processAddToFavorites(Long eventId, UserDetails userDetails, RedirectAttributes redirectAttributes) {
		if (userDetails == null) {
			redirectAttributes.addFlashAttribute("error", "You must be logged in to add favorites.");
			return "redirect:/login";
		}

		try {
			eventService.addToFavorites(eventId, userDetails.getUsername());
			redirectAttributes.addFlashAttribute("message", "Event added to favorites");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}

		return "redirect:/events/" + eventId;
	}

	@Override
	public String processRemoveFromFavorites(Long eventId, UserDetails userDetails,
			RedirectAttributes redirectAttributes) {
		if (userDetails == null) {
			redirectAttributes.addFlashAttribute("error", "You must be logged in to remove favorites.");
			return "redirect:/login";
		}

		try {
			eventService.removeFromFavorites(eventId, userDetails.getUsername());
			redirectAttributes.addFlashAttribute("message", "Event removed from favorites");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}

		return "redirect:/user/favorites";
	}
}