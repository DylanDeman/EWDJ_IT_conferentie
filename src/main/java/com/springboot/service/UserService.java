package com.springboot.service;

import com.springboot.domain.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getAllUsers();
    Optional<User> getUserById(Long id);
    Optional<User> getUserByUsername(String username);
    User createUser(User user);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    boolean existsByUsername(String username);
    void changePassword(Long userId, String newPassword);
    boolean isAdmin(String username);
    void increaseFailedAttempts(User user);
    void resetFailedAttempts(String username);
    void lock(User user);
    boolean unlockWhenTimeExpired(User user);
} 