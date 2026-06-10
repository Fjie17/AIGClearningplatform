package com.example.learningplatform.test;


	import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

	public class JwtDecodeTest {

	    public static void main(String[] args) {

	        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	        String rawPassword = "admin123";

	        String hash = "$2a$10$8YbLZEbfefxXRnvK7hjbQ.3BfvqowVsqO.QNix2scZdGWD8LD2KZG";

	        boolean result = encoder.matches(rawPassword, hash);

	        System.out.println(result);
	    }
	}