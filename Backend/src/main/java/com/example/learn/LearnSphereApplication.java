package com.example.learn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan
public class LearnSphereApplication {

	public static void main(String[] args) {
		SpringApplication.run(LearnSphereApplication.class, args);
	}

}
