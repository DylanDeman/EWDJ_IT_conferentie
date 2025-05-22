package service;

import domain.Event;
import domain.MyUser;
import exceptions.EventNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.EventRepository;
import repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Optional<Event> findById(Long id) {
        Optional<Event> event = eventRepository.findById(id);
        if (event.isEmpty()) {
            throw new EventNotFoundException("id", id);
        }
        return event;
    }

    @Override
    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    @Override
    public Event save(Event event) {
        return eventRepository.save(event);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        // Check if event exists before deletion
        if (!eventRepository.existsById(id)) {
            throw new EventNotFoundException("id", id);
        }

        List<MyUser> usersWithFavorite = userRepository.findAll().stream()
                .filter(user -> user.getFavorites().stream()
                        .anyMatch(event -> event.getId().equals(id)))
                .toList();
        
        for (MyUser user : usersWithFavorite) {
            Set<Event> updatedFavorites = user.getFavorites().stream()
                    .filter(event -> !event.getId().equals(id))
                    .collect(Collectors.toSet());
            
            user.setFavorites(updatedFavorites);
            userRepository.save(user);
        }
        
        eventRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Event> getEventsByDate(LocalDate date) {
        List<Event> events = eventRepository.findByDateOrderByDateTimeAndName(date);
        
        return events.stream()
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                Event::getId,
                                event -> event,
                                (existing, replacement) -> existing
                        ),
                        map -> new java.util.ArrayList<>(map.values())
                ));
    }
}