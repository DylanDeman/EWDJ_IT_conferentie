package service;

import domain.MyUser;

public interface UserService {
	MyUser findByUsername(String name);
}