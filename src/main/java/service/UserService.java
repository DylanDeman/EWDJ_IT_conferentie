package service;

import java.util.List;

import domain.MyUser;

public interface UserService {
	MyUser findByUsername(String name);

	List<MyUser> findAll();

}