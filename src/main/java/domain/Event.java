package domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = "id")
@Getter
@Setter
@Table(name = "events")
public class Event implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private String description;

	@ElementCollection
	@Builder.Default
	@CollectionTable(name = "event_speakers", joinColumns = @JoinColumn(name = "event_id"))
	@Column(name = "speaker")
	@Size(max = 3, message = "An event can have 3 speakers at most.")
	private List<String> speakers = new ArrayList<>();

	@ManyToOne
	@JoinColumn(name = "room_id")
	private Room room;

	private LocalDateTime dateTime;

	private int beamerCode;

	@Column(nullable = false)
	private String beamerCheck;

	@Column(nullable = false)
	private BigDecimal price;

	@Builder.Default
	@ManyToMany(mappedBy = "favorites")
	private Set<User> userFavorites = new HashSet<>();

}