package service;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;

import domain.Event;
import domain.MyUser;

public interface UserService {
	MyUser findByUsername(String name);

	List<MyUser> findAll();

	MyUser save(MyUser myUser);

	boolean isAdmin(String username);

	List<Event> getSortedUserFavorites(String username);

	String prepareUserFavoritesModel(Model model, UserDetails userDetails);
}