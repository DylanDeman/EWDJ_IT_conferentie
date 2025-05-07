package com.springboot.domain;

import com.springboot.validation.ConferencePeriod;
import com.springboot.validation.ValidBeamerCheck;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "events", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "dateTime"}),
    @UniqueConstraint(columnNames = {"room_id", "dateTime"})
})
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{event.name.required}")
    @Pattern(regexp = "^[a-zA-Z].*", message = "{event.name.start.letter}")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(name = "event_speakers", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "speaker")
    @Size(min = 1, max = 3, message = "{event.speakers.size}")
    private Set<String> speakers = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @NotNull(message = "{event.room.required}")
    private Room room;

    @NotNull(message = "{event.datetime.required}")
    @Future(message = "{event.datetime.future}")
    @ConferencePeriod
    private LocalDateTime dateTime;

    @Column(name = "beamer_code")
    @Pattern(regexp = "^\\d{4}$", message = "{event.beamer.code.format}")
    private String beamerCode;

    @Column(name = "beamer_check")
    @Pattern(regexp = "^\\d{2}$", message = "{event.beamer.check.format}")
    private String beamerCheck;

    @NotNull(message = "{event.price.required}")
    @DecimalMin(value = "9.99", message = "{event.price.min}")
    @DecimalMax(value = "99.99", message = "{event.price.max}")
    @Digits(integer = 2, fraction = 2, message = "{event.price.format}")
    private BigDecimal price;

    @ManyToMany(mappedBy = "favoriteEvents")
    private Set<User> favoritedBy = new HashSet<>();

    @Version
    private Long version;

    public boolean validateBeamerCheck() {
        if (beamerCode == null || beamerCheck == null) {
            return false;
        }
        try {
            int code = Integer.parseInt(beamerCode);
            int check = Integer.parseInt(beamerCheck);
            return check == (code % 97);
        } catch (NumberFormatException e) {
            return false;
        }
    }
} 