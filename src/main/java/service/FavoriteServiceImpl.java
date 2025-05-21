package service;

import domain.Event;
import domain.MyUser;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.EventRepository;
import repository.UserRepository;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Value("${app.favorites.limit:5}")
    private int favoritesLimit;

    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public void toggleFavorite(Long eventId, String username) {
        if (eventId == null) {
            throw new IllegalArgumentException("Event ID is required");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + eventId));

        MyUser user = userRepository.findByUsername(username);
        if (user == null) {
            throw new EntityNotFoundException("User not found: " + username);
        }

        boolean isFavorite = user.getFavorites().contains(event);
        if (isFavorite) {
            user.getFavorites().remove(event);
        } else {
            if (user.getFavorites().size() >= favoritesLimit) {
                throw new IllegalStateException("You can only have " + favoritesLimit + " favorites");
            }
            user.getFavorites().add(event);
        }

        userRepository.save(user);
    }
}