package com.springboot.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{room.name.required}")
    @Pattern(regexp = "^[a-zA-Z]\\d{3}$", message = "{room.name.format}")
    @Column(unique = true)
    private String name;

    @NotNull(message = "{room.capacity.required}")
    @Min(value = 1, message = "{room.capacity.min}")
    @Max(value = 50, message = "{room.capacity.max}")
    private Integer capacity;

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private Set<Event> events = new HashSet<>();

    @Version
    private Long version;
} 