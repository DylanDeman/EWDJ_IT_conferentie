package service;

import java.util.List;

import domain.Speaker;

public interface SpeakerService {
	List<Speaker> findAll();

	Speaker findById(Long id);
}
