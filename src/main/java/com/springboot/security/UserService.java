package com.springboot.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springboot.exception.UserNotFoundException;
import com.springboot.repository.UserRepository;

import domain.User;

@Service
public class UserService {
	@Autowired
	UserRepository UserRepository;

	public User getUserByUsername(String username) {
		User user = UserRepository.findByUsername(username);
		if (user == null) {
			throw new UserNotFoundException();
		}
		return user;
	}
}