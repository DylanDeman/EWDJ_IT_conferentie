package com.springboot.repository;

import com.springboot.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.failedAttempt = ?1 WHERE u.username = ?2")
    void updateFailedAttempts(int failAttempts, String username);

    @Modifying
    @Query("UPDATE User u SET u.accountNonLocked = false, u.lockTime = ?2 WHERE u.username = ?1")
    void lock(String username, LocalDateTime lockTime);

    @Modifying
    @Query("UPDATE User u SET u.accountNonLocked = true, u.lockTime = null, u.failedAttempt = 0 WHERE u.username = ?1")
    void unlock(String username);
} 