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
		Room roomA = Room.builder().name("Room A").capacity(50).build();
		Room roomB = Room.builder().name("Room B").capacity(30).build();
		Room roomC = Room.builder().name("Room C").capacity(100).build();
		Room roomD = Room.builder().name("Room D").capacity(20).build();
		Room roomE = Room.builder().name("Room E").capacity(40).build();

		roomRepository.saveAll(List.of(roomA, roomB, roomC, roomD, roomE));

		// Create speakers
		Speaker aliceJohnson = Speaker.builder().speakerName("Alice Johnson").build();
		Speaker bobSmith = Speaker.builder().speakerName("Bob Smith").build();
		Speaker charlieBaker = Speaker.builder().speakerName("Charlie Baker").build();
		Speaker carolWhite = Speaker.builder().speakerName("Carol White").build();
		Speaker davidGreen = Speaker.builder().speakerName("David Green").build();
		Speaker erinLee = Speaker.builder().speakerName("Erin Lee").build();
		Speaker frankNovak = Speaker.builder().speakerName("Frank Novak").build();
		Speaker graceKim = Speaker.builder().speakerName("Grace Kim").build();
		Speaker harryStone = Speaker.builder().speakerName("Harry Stone").build();
		Speaker ireneWoods = Speaker.builder().speakerName("Irene Woods").build();
		Speaker jamesTan = Speaker.builder().speakerName("James Tan").build();
		Speaker kellyZhang = Speaker.builder().speakerName("Kelly Zhang").build();

		speakerRepository.saveAll(List.of(aliceJohnson, bobSmith, charlieBaker, carolWhite, davidGreen, erinLee,
				frankNovak, graceKim, harryStone, ireneWoods, jamesTan, kellyZhang));

		// Shared time slots (parallel sessions)
		LocalDateTime slot1 = LocalDateTime.now().plusDays(3).withHour(10).withMinute(0);
		LocalDateTime slot2 = LocalDateTime.now().plusDays(3).withHour(13).withMinute(0);
		LocalDateTime slot3 = LocalDateTime.now().plusDays(4).withHour(11).withMinute(0);

		// Events with actual Speaker objects
		Event event1 = Event.builder().name("Spring Boot Workshop")
				.description("A workshop on Spring Boot fundamentals")
				.speakers(List.of(aliceJohnson, bobSmith, charlieBaker)).room(roomA).dateTime(slot1).beamerCode(1234)
				.beamerCheck(1).price(new BigDecimal("49.99")).build();

		Event event2 = Event.builder().name("Thymeleaf Deep Dive")
				.description("In-depth session on Thymeleaf templating engine").speakers(List.of(carolWhite))
				.room(roomB).dateTime(slot1) // Same time as event1, different room
				.beamerCode(5678).beamerCheck(1).price(new BigDecimal("39.99")).build();

		Event event3 = Event.builder().name("Security in Spring")
				.description("Spring Security essentials and best practices").speakers(List.of(davidGreen)).room(roomC)
				.dateTime(slot2).beamerCode(1111).beamerCheck(1).price(new BigDecimal("59.99")).build();

		Event event4 = Event.builder().name("REST API Design")
				.description("How to design clean REST APIs with Spring Boot").speakers(List.of(erinLee)).room(roomA)
				.dateTime(slot2) // Same time as event3, different room
				.beamerCode(2222).beamerCheck(1).price(new BigDecimal("44.99")).build();

		Event event5 = Event.builder().name("Docker for Java Developers")
				.description("Learn Docker basics and how to containerize Spring apps").speakers(List.of(frankNovak))
				.room(roomD).dateTime(slot3).beamerCode(3333).beamerCheck(1).price(new BigDecimal("34.99")).build();

		Event event6 = Event.builder().name("Kubernetes 101").description("Get started with Kubernetes orchestration")
				.speakers(List.of(graceKim)).room(roomE).dateTime(slot3) // Same time as event5, different room
				.beamerCode(4444).beamerCheck(1).price(new BigDecimal("69.99")).build();

		Event event7 = Event.builder().name("Microservices Architecture")
				.description("Building scalable microservices with Spring Cloud").speakers(List.of(harryStone))
				.room(roomC).dateTime(LocalDateTime.now().plusDays(5).withHour(10)).beamerCode(5555).beamerCheck(1)
				.price(new BigDecimal("79.99")).build();

		Event event8 = Event.builder().name("CI/CD Pipelines with GitHub Actions")
				.description("Automate your deployment pipelines using GitHub Actions").speakers(List.of(ireneWoods))
				.room(roomB).dateTime(LocalDateTime.now().plusDays(6).withHour(9)).beamerCode(6666).beamerCheck(1)
				.price(new BigDecimal("29.99")).build();

		Event event9 = Event.builder().name("Advanced JPA & Hibernate")
				.description("Performance tuning and advanced mappings with JPA").speakers(List.of(jamesTan))
				.room(roomA) // same room reused
				.dateTime(LocalDateTime.now().plusDays(6).withHour(11)).beamerCode(7777).beamerCheck(1)
				.price(new BigDecimal("54.99")).build();

		Event event10 = Event.builder().name("Java 21 Features").description("Explore the latest features in Java 21")
				.speakers(List.of(kellyZhang)).room(roomE) // same room reused
				.dateTime(LocalDateTime.now().plusDays(7).withHour(14)).beamerCode(8888).beamerCheck(1)
				.price(new BigDecimal("64.99")).build();

		eventRepository
				.saveAll(List.of(event1, event2, event3, event4, event5, event6, event7, event8, event9, event10));
	}
}
