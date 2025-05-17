package com.example.learn.Service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.learn.Entity.User;
import com.example.learn.Repository.UserRepository;

@Service
public class RegistrationService {

	private final UserRepository userRepository;
	private final BCryptPasswordEncoder passwordEncoder;

	public RegistrationService(UserRepository userRepository) {
		this.userRepository = userRepository;
		this.passwordEncoder = new BCryptPasswordEncoder();
	}

	public boolean checkEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	public boolean checkName(String name) {
		return userRepository.existsByName(name);
	}

	public void addUser(User user) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		userRepository.save(user);
	}
}
