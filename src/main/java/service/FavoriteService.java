package service;


import org.springframework.transaction.annotation.Transactional;


public interface FavoriteService {
    // Add the toggleFavorite method to the interface
    @Transactional
    void toggleFavorite(Long eventId, String username);
}