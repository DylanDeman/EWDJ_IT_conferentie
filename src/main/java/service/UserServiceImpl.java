package service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import domain.Event;
import domain.MyUser;
import repository.UserRepository;
import util.Role;

@Service
public class UserServiceImpl implements UserService {
	@Autowired
	private UserRepository userRepository;

	@Override
	public MyUser findByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	@Override
	public List<MyUser> findAll() {
		return userRepository.findAll();
	}

	@Override
	public boolean isAdmin(String username) {
		MyUser user = findByUsername(username);
		return user != null && user.getRole() == Role.ADMIN;
	}

	@Override
	public List<Event> getSortedUserFavorites(String username) {
		MyUser user = findByUsername(username);

		if (user == null) {
			return List.of();
		}

		return user.getFavorites().stream()
				.sorted(Comparator.comparing(Event::getDateTime).thenComparing(Event::getName))
				.collect(Collectors.toList());
	}

	@Override
	public String prepareUserFavoritesModel(Model model, UserDetails userDetails) {
		if (userDetails == null) {
			return "redirect:/login";
		}

		List<Event> sortedFavorites = getSortedUserFavorites(userDetails.getUsername());
		model.addAttribute("favorites", sortedFavorites);

		return "user/favorites";
	}
}