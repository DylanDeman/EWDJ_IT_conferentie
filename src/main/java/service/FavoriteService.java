package service;


import org.springframework.transaction.annotation.Transactional;


public interface FavoriteService {
    @Transactional
    void toggleFavorite(Long eventId, String username);
}