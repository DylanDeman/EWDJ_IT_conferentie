package com.springboot.service;

import java.util.List;
import java.util.Optional;

import domain.User;

public interface UserService {
	List<User> getAllUsers();

	Optional<User> getUserById(Long id);

	User getUserByUsername(String username);

	User createUser(User user);

	User updateUser(Long id, User user);

	void deleteUser(Long id);

	boolean existsByUsername(String username);

	void changePassword(Long userId, String newPassword);

	boolean isAdmin(String username);

}