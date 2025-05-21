package com.springboot.EWDJ_IT_conferentie;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

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
	private  SpeakerRepository speakerRepository;

	private final PasswordEncoder encoder = new BCryptPasswordEncoder();
	private final Random random = new Random();


	@Override
	public void run(String... args) throws Exception {
		List<MyUser> users = new ArrayList<>();
		users.add(MyUser.builder().username("admin").password(encoder.encode("password")).role(Role.ADMIN).build());
		users.add(MyUser.builder().username("johnadmin").password(encoder.encode("password")).role(Role.ADMIN).build());
		users.add(MyUser.builder().username("Alice").password(encoder.encode("password")).role(Role.USER).build());
		users.add(MyUser.builder().username("Bob").password(encoder.encode("password")).role(Role.USER).build());
		users.add(MyUser.builder().username("Charlie").password(encoder.encode("password")).role(Role.USER).build());
		users.add(MyUser.builder().username("Diana").password(encoder.encode("password")).role(Role.USER).build());
		users.add(MyUser.builder().username("Ethan").password(encoder.encode("password")).role(Role.USER).build());
		users.add(MyUser.builder().username("Fiona").password(encoder.encode("password")).role(Role.USER).build());
		users.add(MyUser.builder().username("George").password(encoder.encode("password")).role(Role.USER).build());
		users.add(MyUser.builder().username("Hannah").password(encoder.encode("password")).role(Role.USER).build());
		userRepository.saveAll(users);

		List<Room> rooms = new ArrayList<>();
		rooms.add(Room.builder().name("A101").capacity(30).build());
		rooms.add(Room.builder().name("A102").capacity(40).build());
		rooms.add(Room.builder().name("A103").capacity(50).build());
		rooms.add(Room.builder().name("B201").capacity(25).build());
		rooms.add(Room.builder().name("B202").capacity(35).build());
		rooms.add(Room.builder().name("B203").capacity(45).build());
		rooms.add(Room.builder().name("C301").capacity(20).build());
		rooms.add(Room.builder().name("C302").capacity(30).build());
		rooms.add(Room.builder().name("C303").capacity(40).build());
		rooms.add(Room.builder().name("D401").capacity(15).build());
		rooms.add(Room.builder().name("D402").capacity(25).build());
		rooms.add(Room.builder().name("D403").capacity(35).build());
		roomRepository.saveAll(rooms);

		List<Speaker> speakers = new ArrayList<>();
		speakers.add(Speaker.builder().name("Alice Johnson").build());
		speakers.add(Speaker.builder().name("Bob Smith").build());
		speakers.add(Speaker.builder().name("Charlie Baker").build());
		speakers.add(Speaker.builder().name("Diana Wong").build());
		speakers.add(Speaker.builder().name("Edward Kim").build());
		speakers.add(Speaker.builder().name("Fiona Chen").build());
		speakers.add(Speaker.builder().name("George Rodriguez").build());
		speakers.add(Speaker.builder().name("Hannah Davis").build());
		speakers.add(Speaker.builder().name("Ian Patel").build());
		speakers.add(Speaker.builder().name("Julia Nguyen").build());
		speakers.add(Speaker.builder().name("Kevin Lee").build());
		speakers.add(Speaker.builder().name("Laura Wilson").build());
		speakers.add(Speaker.builder().name("Michael Brown").build());
		speakers.add(Speaker.builder().name("Nina Garcia").build());
		speakers.add(Speaker.builder().name("Oscar Martinez").build());
		speakers.add(Speaker.builder().name("Priya Sharma").build());
		speakers.add(Speaker.builder().name("Quincy Taylor").build());
		speakers.add(Speaker.builder().name("Rachel Green").build());
		speakers.add(Speaker.builder().name("Samuel Jackson").build());
		speakers.add(Speaker.builder().name("Tina Turner").build());

		// Adding 5 more speakers for more diversity
		speakers.add(Speaker.builder().name("Uma Thurman").build());
		speakers.add(Speaker.builder().name("Victor Hugo").build());
		speakers.add(Speaker.builder().name("Wendy Williams").build());
		speakers.add(Speaker.builder().name("Xavier Chen").build());
		speakers.add(Speaker.builder().name("Yasmine Ali").build());

		speakerRepository.saveAll(speakers);

		List<LocalDateTime> timeSlots = new ArrayList<>();
		for (int day = 1; day <= 10; day++) { // Extended to 10 days to accommodate more events
			timeSlots.add(LocalDateTime.of(2025, 6, day, 9, 0));
			timeSlots.add(LocalDateTime.of(2025, 6, day, 11, 0));
			timeSlots.add(LocalDateTime.of(2025, 6, day, 14, 0));
			timeSlots.add(LocalDateTime.of(2025, 6, day, 16, 0));
		}

		List<Event> events = new ArrayList<>();

		// Original events with 1-2 speakers
		events.add(Event.builder().name("Spring Boot Fundamentals")
				.description("An introduction to Spring Boot core concepts")
				.speakers(List.of(speakers.get(0), speakers.get(1))).room(rooms.get(0)).dateTime(timeSlots.get(0))
				.beamerCode(1234).beamerCheck(1234 % 97).price(new BigDecimal("49.99")).build());

		events.add(Event.builder().name("Reactive Programming with WebFlux")
				.description("Building responsive applications with Spring WebFlux").speakers(List.of(speakers.get(2)))
				.room(rooms.get(1)).dateTime(timeSlots.get(0)).beamerCode(2345).beamerCheck(2345 % 97)
				.price(new BigDecimal("39.99")).build());

		events.add(Event.builder().name("Advanced Microservices")
				.description("Design patterns for microservice architecture")
				.speakers(List.of(speakers.get(3), speakers.get(4))).room(rooms.get(2)).dateTime(timeSlots.get(1))
				.beamerCode(3456).beamerCheck(3456 % 97).price(new BigDecimal("59.99")).build());

		events.add(Event.builder().name("Docker and Kubernetes")
				.description("Containerization and orchestration for Java applications")
				.speakers(List.of(speakers.get(5))).room(rooms.get(3)).dateTime(timeSlots.get(1)).beamerCode(4567)
				.beamerCheck(4567 % 97).price(new BigDecimal("69.99")).build());

		events.add(Event.builder().name("Cloud-Native Java").description("Building applications for the cloud")
				.speakers(List.of(speakers.get(6), speakers.get(7))).room(rooms.get(4)).dateTime(timeSlots.get(2))
				.beamerCode(5678).beamerCheck(5678 % 97).price(new BigDecimal("54.99")).build());

		events.add(Event.builder().name("Spring Security in Depth")
				.description("Authentication, authorization, and beyond").speakers(List.of(speakers.get(8)))
				.room(rooms.get(5)).dateTime(timeSlots.get(2)).beamerCode(6789).beamerCheck(6789 % 97)
				.price(new BigDecimal("44.99")).build());

		events.add(Event.builder().name("Test-Driven Development").description("Best practices for TDD in Java")
				.speakers(List.of(speakers.get(9), speakers.get(10))).room(rooms.get(6)).dateTime(timeSlots.get(3))
				.beamerCode(7890).beamerCheck(7890 % 97).price(new BigDecimal("34.99")).build());

		events.add(Event.builder().name("RESTful API Design").description("Building elegant and scalable APIs")
				.speakers(List.of(speakers.get(11))).room(rooms.get(7)).dateTime(timeSlots.get(3)).beamerCode(8901)
				.beamerCheck(8901 % 97).price(new BigDecimal("29.99")).build());

		events.add(Event.builder().name("Advanced Hibernate").description("Performance tuning and advanced mappings")
				.speakers(List.of(speakers.get(12), speakers.get(13))).room(rooms.get(8)).dateTime(timeSlots.get(4))
				.beamerCode(9012).beamerCheck(9012 % 97).price(new BigDecimal("64.99")).build());

		events.add(Event.builder().name("GraphQL with Spring").description("Building flexible APIs with GraphQL")
				.speakers(List.of(speakers.get(14))).room(rooms.get(9)).dateTime(timeSlots.get(4)).beamerCode(1357)
				.beamerCheck(1357 % 97).price(new BigDecimal("74.99")).build());

		events.add(Event.builder().name("DevOps for Java Developers").description("CI/CD pipelines and automation")
				.speakers(List.of(speakers.get(15), speakers.get(16))).room(rooms.get(10)).dateTime(timeSlots.get(5))
				.beamerCode(2468).beamerCheck(2468 % 97).price(new BigDecimal("59.99")).build());

		events.add(Event.builder().name("Java Performance Tuning").description("Finding and fixing bottlenecks")
				.speakers(List.of(speakers.get(17))).room(rooms.get(11)).dateTime(timeSlots.get(5)).beamerCode(3698)
				.beamerCheck(3698 % 97).price(new BigDecimal("49.99")).build());

		events.add(Event.builder().name("Kotlin for Java Developers").description("Transitioning to Kotlin from Java")
				.speakers(List.of(speakers.get(18), speakers.get(19))).room(rooms.get(0)).dateTime(timeSlots.get(6))
				.beamerCode(7531).beamerCheck(7531 % 97).price(new BigDecimal("39.99")).build());

		events.add(Event.builder().name("Clean Code Principles").description("Writing maintainable and readable code")
				.speakers(List.of(speakers.get(0), speakers.get(19))).room(rooms.get(1)).dateTime(timeSlots.get(6))
				.beamerCode(8642).beamerCheck(8642 % 97).price(new BigDecimal("44.99")).build());

		events.add(Event.builder().name("Event-Driven Architecture").description("Building systems with event streams")
				.speakers(List.of(speakers.get(1), speakers.get(18))).room(rooms.get(2)).dateTime(timeSlots.get(7))
				.beamerCode(9753).beamerCheck(9753 % 97).price(new BigDecimal("54.99")).build());

		events.add(Event.builder().name("Domain-Driven Design")
				.description("Strategic and tactical patterns for complex domains")
				.speakers(List.of(speakers.get(2), speakers.get(3))).room(rooms.get(3)).dateTime(timeSlots.get(8))
				.beamerCode(1470).beamerCheck(1470 % 97).price(new BigDecimal("69.99")).build());

		events.add(Event.builder().name("Spring Data Deep Dive").description("Advanced data access with Spring Data")
				.speakers(List.of(speakers.get(4))).room(rooms.get(4)).dateTime(timeSlots.get(8)).beamerCode(2581)
				.beamerCheck(2581 % 97).price(new BigDecimal("49.99")).build());

		events.add(Event.builder().name("Java 21 Features").description("New features in Java 21")
				.speakers(List.of(speakers.get(5), speakers.get(6))).room(rooms.get(5)).dateTime(timeSlots.get(12))
				.beamerCode(3692).beamerCheck(3692 % 97).price(new BigDecimal("54.99")).build());

		events.add(Event.builder().name("Machine Learning for Java Developers")
				.description("Introduction to ML libraries for Java").speakers(List.of(speakers.get(7)))
				.room(rooms.get(6)).dateTime(timeSlots.get(16)).beamerCode(4803).beamerCheck(4803 % 97)
				.price(new BigDecimal("79.99")).build());

		events.add(Event.builder().name("Web Security Fundamentals").description("Protecting your web applications")
				.speakers(List.of(speakers.get(8), speakers.get(9))).room(rooms.get(7)).dateTime(timeSlots.get(20))
				.beamerCode(5914).beamerCheck(5914 % 97).price(new BigDecimal("64.99")).build());

		events.add(Event.builder().name("Serverless Java").description("Building serverless applications with Java")
				.speakers(List.of(speakers.get(10))).room(rooms.get(8)).dateTime(timeSlots.get(24)).beamerCode(6025)
				.beamerCheck(6025 % 97).price(new BigDecimal("69.99")).build());

		// New events with 3 speakers
		events.add(Event.builder().name("Full Stack Java Development")
				.description("Comprehensive approach to full stack development with Java and modern frontend frameworks")
				.speakers(List.of(speakers.get(0), speakers.get(5), speakers.get(10)))
				.room(rooms.get(9)).dateTime(timeSlots.get(9))
				.beamerCode(7136).beamerCheck(7136 % 97)
				.price(new BigDecimal("89.99")).build());

		events.add(Event.builder().name("AI and Machine Learning with Java")
				.description("Implementing advanced AI algorithms and machine learning models in Java")
				.speakers(List.of(speakers.get(1), speakers.get(6), speakers.get(11)))
				.room(rooms.get(10)).dateTime(timeSlots.get(10))
				.beamerCode(8247).beamerCheck(8247 % 97)
				.price(new BigDecimal("99.99")).build());

		events.add(Event.builder().name("Advanced Spring Ecosystem")
				.description("Deep dive into the Spring ecosystem - Spring Boot, Cloud, Security, and Data")
				.speakers(List.of(speakers.get(2), speakers.get(7), speakers.get(12)))
				.room(rooms.get(11)).dateTime(timeSlots.get(11))
				.beamerCode(9358).beamerCheck(9358 % 97)
				.price(new BigDecimal("79.99")).build());

		events.add(Event.builder().name("High-Performance Java Applications")
				.description("Techniques for building high-performance, scalable Java applications")
				.speakers(List.of(speakers.get(3), speakers.get(8), speakers.get(13)))
				.room(rooms.get(0)).dateTime(timeSlots.get(13))
				.beamerCode(1469).beamerCheck(1469 % 97)
				.price(new BigDecimal("84.99")).build());

		events.add(Event.builder().name("Java Security Masterclass")
				.description("Comprehensive security principles and implementation for Java applications")
				.speakers(List.of(speakers.get(4), speakers.get(9), speakers.get(14)))
				.room(rooms.get(1)).dateTime(timeSlots.get(14))
				.beamerCode(2570).beamerCheck(2570 % 97)
				.price(new BigDecimal("94.99")).build());

		events.add(Event.builder().name("Quantum Computing with Java")
				.description("Introduction to quantum computing algorithms and Java libraries for quantum computing")
				.speakers(List.of(speakers.get(15), speakers.get(20), speakers.get(21)))
				.room(rooms.get(2)).dateTime(timeSlots.get(15))
				.beamerCode(3681).beamerCheck(3681 % 97)
				.price(new BigDecimal("109.99")).build());

		events.add(Event.builder().name("Blockchain Development in Java")
				.description("Creating blockchain applications with Java")
				.speakers(List.of(speakers.get(16), speakers.get(22), speakers.get(23)))
				.room(rooms.get(3)).dateTime(timeSlots.get(17))
				.beamerCode(4792).beamerCheck(4792 % 97)
				.price(new BigDecimal("89.99")).build());

		events.add(Event.builder().name("Java for Big Data Processing")
				.description("Utilizing Java frameworks for big data processing and analysis")
				.speakers(List.of(speakers.get(17), speakers.get(18), speakers.get(24)))
				.room(rooms.get(4)).dateTime(timeSlots.get(18))
				.beamerCode(5903).beamerCheck(5903 % 97)
				.price(new BigDecimal("79.99")).build());

		events.add(Event.builder().name("Agile Development with Java")
				.description("Best practices for agile development in Java projects")
				.speakers(List.of(speakers.get(19), speakers.get(20), speakers.get(21)))
				.room(rooms.get(5)).dateTime(timeSlots.get(19))
				.beamerCode(6014).beamerCheck(6014 % 97)
				.price(new BigDecimal("64.99")).build());

		events.add(Event.builder().name("Internet of Things with Java")
				.description("Building IoT solutions using Java and related technologies")
				.speakers(List.of(speakers.get(22), speakers.get(23), speakers.get(24)))
				.room(rooms.get(6)).dateTime(timeSlots.get(21))
				.beamerCode(7125).beamerCheck(7125 % 97)
				.price(new BigDecimal("74.99")).build());

		eventRepository.saveAll(events);
		setFavoritesForUsers(users, events);
	}

	private void setFavoritesForUsers(List<MyUser> users, List<Event> events) {
		final int MAX_FAVORITES = 15; // Increased maximum favorites

		// Track which users will have more favorites
		boolean[] powerUsers = {false, false, true, false, true, true, false, true, false, false};

		int userIndex = 0;
		for (MyUser user : users) {
			if (user.getRole() == Role.ADMIN) {
				continue;
			}

			List<Event> availableEvents = new ArrayList<>(events);
			int numFavorites;

			// Power users get many more favorites
			if (userIndex < powerUsers.length && powerUsers[userIndex]) {
				numFavorites = random.nextInt(MAX_FAVORITES - 8) + 8; // 8-15 favorites
			} else {
				numFavorites = random.nextInt(4) + 1; // 1-4 favorites for regular users
			}

			if (user.getFavorites() == null) {
				user.setFavorites(new HashSet<>());
			}

			for (int i = 0; i < numFavorites && !availableEvents.isEmpty(); i++) {
				int index = random.nextInt(availableEvents.size());
				user.getFavorites().add(availableEvents.remove(index));
			}

			userRepository.save(user);
			userIndex++;
		}
	}
}