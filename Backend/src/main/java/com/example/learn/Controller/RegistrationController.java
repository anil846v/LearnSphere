package com.example.learn.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.learn.Entity.User;
import com.example.learn.Service.RegistrationService;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://127.0.0.1:5501", allowCredentials = "true")
public class RegistrationController {

	@Autowired
	private RegistrationService userService;

	public RegistrationController(RegistrationService userService) {
		this.userService = userService;
	}

	@PostMapping("/addUser")
	public Map<String, String> registerUser(@RequestBody User user) {
		Map<String, String> response = new HashMap<>();

		// Check if email already exists
		if (userService.checkEmail(user.getEmail())) {
			System.out.println("Email already exists: " + user.getEmail());
			response.put("status", "error");
			response.put("message", "Email already exists");
			response.put("redirect", "/register");
			return response;
		}
		if (userService.checkName(user.getName())) {
			response.put("status", "error");
			response.put("message", "Name already exists");
			response.put("redirect", "/register");
			return response;
		}

		// Save the user
		userService.addUser(user);

		response.put("status", "success");
		response.put("message", "User registered successfully");
		response.put("redirect", "/login.html");
		return response;
	}
}