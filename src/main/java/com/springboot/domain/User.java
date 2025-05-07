package com.springboot.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_username", columnList = "username", unique = true)
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{user.username.required}")
    @Size(min = 3, max = 50, message = "{user.username.size}")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "{user.username.pattern}")
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @NotBlank(message = "{user.password.required}")
    @Size(min = 8, message = "{user.password.size}")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$", 
             message = "{user.password.pattern}")
    @Column(nullable = false)
    private String password;

    @Email(message = "{user.email.valid}")
    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "{user.role.required}")
    @Column(nullable = false)
    private UserRole role;

    @Column(name = "account_non_expired")
    private boolean accountNonExpired = true;

    @Column(name = "account_non_locked")
    private boolean accountNonLocked = true;

    @Column(name = "credentials_non_expired")
    private boolean credentialsNonExpired = true;

    @Column(name = "enabled")
    private boolean enabled = true;

    @Column(name = "failed_attempt")
    private int failedAttempt = 0;

    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_favorites",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private Set<Event> favoriteEvents = new HashSet<>();

    @Version
    private Long version;

    public enum UserRole {
        ADMIN,
        USER
    }

    @PrePersist
    @PreUpdate
    public void validate() {
        if (username != null) {
            username = username.toLowerCase();
        }
        if (email != null) {
            email = email.toLowerCase();
        }
    }
} 