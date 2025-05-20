package service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public interface FavoriteService {
	String processAddToFavorites(Long eventId, UserDetails userDetails, RedirectAttributes redirectAttributes);

	String processRemoveFromFavorites(Long eventId, UserDetails userDetails, RedirectAttributes redirectAttributes);
}