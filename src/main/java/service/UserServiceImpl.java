package service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import domain.MyUser;
import repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public MyUser findByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	@Override
	public List<MyUser> findAll() {

		return userRepository.findAll();
	}

}
