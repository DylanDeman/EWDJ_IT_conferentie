package com.springboot.service;

import com.springboot.domain.User;

import jakarta.persistence.EntityNotFoundException;
import repository.UserRepository;
import service.UserDetailsServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserDetailsServiceImpl userService;

    private MyUser testUser;

    @BeforeEach
    void setUp() {
        testUser = new MyUser();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");
        testUser.setRole(MyUser.UserRole.USER);
    }

    @Test
    void createUser_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(MyUser.class))).thenReturn(testUser);

        MyUser createdUser = userService.createUser(testUser);

        assertNotNull(createdUser);
        assertEquals("testuser", createdUser.getUsername());
        assertEquals("encodedPassword", createdUser.getPassword());
        verify(userRepository).save(any(MyUser.class));
    }

    @Test
    void createUser_DuplicateUsername() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> userService.createUser(testUser));
    }

    @Test
    void createUser_DuplicateEmail() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> userService.createUser(testUser));
    }

    @Test
    void updateUser_Success() {
        MyUser existingUser = new MyUser();
        existingUser.setId(1L);
        existingUser.setUsername("existinguser");
        existingUser.setEmail("existing@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(MyUser.class))).thenReturn(testUser);

        MyUser updatedUser = userService.updateUser(1L, testUser);

        assertNotNull(updatedUser);
        assertEquals(1L, updatedUser.getId());
        verify(userRepository).save(any(MyUser.class));
    }

    @Test
    void updateUser_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.updateUser(1L, testUser));
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> userService.deleteUser(1L));
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_NotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> userService.deleteUser(1L));
    }

    @Test
    void increaseFailedAttempts_LockAccount() {
        testUser.setFailedAttempt(2);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        userService.increaseFailedAttempts(testUser);

        verify(userRepository).updateFailedAttempts(3, testUser.getUsername());
        verify(userRepository).lock(eq(testUser.getUsername()), any(LocalDateTime.class));
    }

    @Test
    void unlockWhenTimeExpired_Success() {
        testUser.setLockTime(LocalDateTime.now().minusMinutes(1));
        testUser.setAccountNonLocked(false);
        testUser.setFailedAttempt(3);

        when(userRepository.save(any(MyUser.class))).thenReturn(testUser);

        boolean unlocked = userService.unlockWhenTimeExpired(testUser);

        assertTrue(unlocked);
        assertTrue(testUser.isAccountNonLocked());
        assertNull(testUser.getLockTime());
        assertEquals(0, testUser.getFailedAttempt());
    }

    @Test
    void unlockWhenTimeExpired_NotExpired() {
        testUser.setLockTime(LocalDateTime.now().plusMinutes(1));
        testUser.setAccountNonLocked(false);

        boolean unlocked = userService.unlockWhenTimeExpired(testUser);

        assertFalse(unlocked);
        assertFalse(testUser.isAccountNonLocked());
    }
} 