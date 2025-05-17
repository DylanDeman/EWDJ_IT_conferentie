package com.springboot.EWDJ_IT_conferentie;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import domain.Event;
import domain.MyUser;
import domain.Room;
import repository.EventRepository;
import repository.RoomRepository;
import repository.UserRepository;
import util.Role;

@Component
public class InitDataConfig implements CommandLineRunner {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private RoomRepository roomRepository;

	private final PasswordEncoder encoder = new BCryptPasswordEncoder();

	@Override
	public void run(String... args) throws Exception {

		// When creating users:
		MyUser user1 = MyUser.builder().username("alice").password(encoder.encode("password")).role(Role.USER).build();

		MyUser admin = MyUser.builder().username("admin").password(encoder.encode("password")).role(Role.ADMIN).build();

		userRepository.saveAll(List.of(user1, admin));

		Room roomA = Room.builder().name("Room A").capacity(50).build();

		Room roomB = Room.builder().name("Room B").capacity(30).build();

		roomRepository.saveAll(List.of(roomA, roomB));

		Event event1 = Event.builder().name("Spring Boot Workshop")
				.description("A workshop on Spring Boot fundamentals").speakers(List.of("Alice Johnson", "Bob Smith"))
				.room(roomA).dateTime(LocalDateTime.now().plusDays(3)).beamerCode(1234).beamerCheck(1)
				.price(new BigDecimal("49.99")).build();

		Event event2 = Event.builder().name("Thymeleaf Deep Dive")
				.description("In-depth session on Thymeleaf templating engine").speakers(List.of("Carol White"))
				.room(roomB).dateTime(LocalDateTime.now().plusDays(5)).beamerCode(5678).beamerCheck(1)
				.price(new BigDecimal("39.99")).build();

		eventRepository.saveAll(List.of(event1, event2));
	}
}
