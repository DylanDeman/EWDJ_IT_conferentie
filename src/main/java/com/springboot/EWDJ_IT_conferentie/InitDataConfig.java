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
import domain.Speaker;
import repository.EventRepository;
import repository.RoomRepository;
import repository.SpeakerRepository;
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

	@Autowired
	private SpeakerRepository speakerRepository;

	private final PasswordEncoder encoder = new BCryptPasswordEncoder();

	@Override
	public void run(String... args) throws Exception {

		// Create users
		MyUser user1 = MyUser.builder().username("alice").password(encoder.encode("password")).role(Role.USER).build();

		MyUser admin = MyUser.builder().username("admin").password(encoder.encode("password")).role(Role.ADMIN).build();

		userRepository.saveAll(List.of(user1, admin));

		// Create rooms
		Room roomA123 = Room.builder().name("A123").capacity(50).build();
		Room roomB234 = Room.builder().name("B234").capacity(30).build();
		Room roomC345 = Room.builder().name("C345").capacity(45).build();
		Room roomD456 = Room.builder().name("D456").capacity(20).build();
		Room roomE567 = Room.builder().name("E567").capacity(40).build();

		roomRepository.saveAll(List.of(roomA123, roomB234, roomC345, roomD456, roomE567));

		// Create speakers
		Speaker aliceJohnson = Speaker.builder().name("Alice Johnson").build();
		Speaker bobSmith = Speaker.builder().name("Bob Smith").build();
		Speaker charlieBaker = Speaker.builder().name("Charlie Baker").build();
		Speaker carolWhite = Speaker.builder().name("Carol White").build();
		Speaker davidGreen = Speaker.builder().name("David Green").build();
		Speaker erinLee = Speaker.builder().name("Erin Lee").build();
		Speaker frankNovak = Speaker.builder().name("Frank Novak").build();
		Speaker graceKim = Speaker.builder().name("Grace Kim").build();
		Speaker harryStone = Speaker.builder().name("Harry Stone").build();
		Speaker ireneWoods = Speaker.builder().name("Irene Woods").build();
		Speaker jamesTan = Speaker.builder().name("James Tan").build();
		Speaker kellyZhang = Speaker.builder().name("Kelly Zhang").build();

		speakerRepository.saveAll(List.of(aliceJohnson, bobSmith, charlieBaker, carolWhite, davidGreen, erinLee,
				frankNovak, graceKim, harryStone, ireneWoods, jamesTan, kellyZhang));

		// Conference dates (May 1-3, 2025)
		LocalDateTime day1Morning = LocalDateTime.of(2025, 5, 1, 10, 0);
		LocalDateTime day1Afternoon = LocalDateTime.of(2025, 5, 1, 14, 0);
		LocalDateTime day2Morning = LocalDateTime.of(2025, 5, 2, 10, 0);
		LocalDateTime day2Afternoon = LocalDateTime.of(2025, 5, 2, 14, 0);
		LocalDateTime day3Morning = LocalDateTime.of(2025, 5, 3, 10, 0);
		LocalDateTime day3Afternoon = LocalDateTime.of(2025, 5, 3, 14, 0);

		// Events with proper validation
		Event event1 = Event.builder().name("Spring Boot Workshop")
				.description("A workshop on Spring Boot fundamentals").speakers(List.of(aliceJohnson, bobSmith))
				.room(roomA123).dateTime(day1Morning).beamerCode(1234).beamerCheck(12).price(new BigDecimal("49.99"))
				.build();

		Event event2 = Event.builder().name("Thymeleaf Deep Dive")
				.description("In-depth session on Thymeleaf templating engine").speakers(List.of(carolWhite))
				.room(roomB234).dateTime(day1Morning) // Same time as event1, different room
				.beamerCode(5678).beamerCheck(56).price(new BigDecimal("39.99")).build();

		Event event3 = Event.builder().name("Security in Spring")
				.description("Spring Security essentials and best practices").speakers(List.of(davidGreen))
				.room(roomC345).dateTime(day1Afternoon).beamerCode(9012).beamerCheck(90).price(new BigDecimal("59.99"))
				.build();

		Event event4 = Event.builder().name("REST API Design")
				.description("How to design clean REST APIs with Spring Boot").speakers(List.of(erinLee)).room(roomA123)
				.dateTime(day1Afternoon) // Same time as event3, different room
				.beamerCode(3456).beamerCheck(34).price(new BigDecimal("44.99")).build();

		Event event5 = Event.builder().name("Docker for Java Developers")
				.description("Learn Docker basics and how to containerize Spring apps").speakers(List.of(frankNovak))
				.room(roomD456).dateTime(day2Morning).beamerCode(7890).beamerCheck(78).price(new BigDecimal("34.99"))
				.build();

		Event event6 = Event.builder().name("Kubernetes 101").description("Get started with Kubernetes orchestration")
				.speakers(List.of(graceKim)).room(roomE567).dateTime(day2Morning) // Same time as event5, different room
				.beamerCode(2345).beamerCheck(23).price(new BigDecimal("69.99")).build();

		Event event7 = Event.builder().name("Microservices Architecture")
				.description("Building scalable microservices with Spring Cloud").speakers(List.of(harryStone))
				.room(roomC345).dateTime(day2Afternoon).beamerCode(6789).beamerCheck(67).price(new BigDecimal("79.99"))
				.build();

		Event event8 = Event.builder().name("CI/CD Pipelines with GitHub Actions")
				.description("Automate your deployment pipelines using GitHub Actions").speakers(List.of(ireneWoods))
				.room(roomB234).dateTime(day3Morning).beamerCode(1357).beamerCheck(13).price(new BigDecimal("29.99"))
				.build();

		Event event9 = Event.builder().name("Advanced JPA & Hibernate")
				.description("Performance tuning and advanced mappings with JPA").speakers(List.of(jamesTan))
				.room(roomA123) // same room reused
				.dateTime(day3Morning).beamerCode(2468).beamerCheck(24).price(new BigDecimal("54.99")).build();

		Event event10 = Event.builder().name("Java 21 Features").description("Explore the latest features in Java 21")
				.speakers(List.of(kellyZhang)).room(roomE567) // same room reused
				.dateTime(day3Afternoon).beamerCode(9876).beamerCheck(98).price(new BigDecimal("64.99")).build();

		eventRepository
				.saveAll(List.of(event1, event2, event3, event4, event5, event6, event7, event8, event9, event10));
	}
}