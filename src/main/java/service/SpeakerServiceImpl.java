package service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import domain.Speaker;
import repository.SpeakerRepository;

@Service
public class SpeakerServiceImpl implements SpeakerService {

	@Autowired
	SpeakerRepository speakerRepository;

	@Override
	public List<Speaker> findAll() {
		return speakerRepository.findAll();
	}

	@Override
	public Speaker findById(Long id) {
		return speakerRepository.findById(id).orElse(null);
	}
}
