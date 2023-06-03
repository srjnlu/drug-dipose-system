package com.drug.dispose.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.drug.dispose.dto.UserDTO;
import com.drug.dispose.service.UserService;
import com.drug.dispose.util.UserValidation;

@RestController
@RequestMapping("/v1/user")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private UserValidation userValidation;

	@PostMapping("/register")
	public ResponseEntity<?> createUser(@RequestBody UserDTO user) {

		try {
			if (userValidation.userNullCheck(user)) {
				boolean isUserRegistered = userService.registerUser(user);
				if (isUserRegistered) {
					return new ResponseEntity<>(HttpStatus.CREATED);
				} else {
					return new ResponseEntity<>("Something went wrong while registering you", HttpStatus.BAD_GATEWAY);
				}
			} else {
				return new ResponseEntity<>("Please fill all the field", HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			return new ResponseEntity<>("Something went wrong", HttpStatus.BAD_GATEWAY);
		}

	}

}
