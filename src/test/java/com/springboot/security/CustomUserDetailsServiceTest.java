package com.springboot.security;

import com.springboot.domain.User;
import com.springboot.repository.UserRepository;
import com.springboot.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");
        testUser.setRole(User.UserRole.USER);
        testUser.setAccountNonExpired(true);
        testUser.setAccountNonLocked(true);
        testUser.setCredentialsNonExpired(true);
        testUser.setEnabled(true);
    }

    @Test
    void loadUserByUsername_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userService.unlockWhenTimeExpired(testUser)).thenReturn(false);

        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertEquals(1, userDetails.getAuthorities().size());
        assertEquals("ROLE_USER", userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void loadUserByUsername_UserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("nonexistent"));
    }

    @Test
    void loadUserByUsername_AccountLocked() {
        testUser.setAccountNonLocked(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userService.unlockWhenTimeExpired(testUser)).thenReturn(false);

        assertThrows(LockedException.class, () -> userDetailsService.loadUserByUsername("testuser"));
    }

    @Test
    void loadUserByUsername_AccountUnlocked() {
        testUser.setAccountNonLocked(false);
        testUser.setLockTime(LocalDateTime.now().minusMinutes(1));
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userService.unlockWhenTimeExpired(testUser)).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertTrue(userDetails.isAccountNonLocked());
    }

    @Test
    void loadUserByUsername_CaseInsensitive() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userService.unlockWhenTimeExpired(testUser)).thenReturn(false);

        UserDetails userDetails = userDetailsService.loadUserByUsername("TESTUSER");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
    }
} 