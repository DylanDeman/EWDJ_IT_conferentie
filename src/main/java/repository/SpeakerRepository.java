package repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import domain.Speaker;

@Repository
public interface SpeakerRepository extends JpaRepository<Speaker, Long> {
	// You can add custom query methods here if needed
}
