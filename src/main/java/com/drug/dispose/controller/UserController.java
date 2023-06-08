package com.drug.dispose.controller;

import org.slf4j.LoggerFactory;
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

import ch.qos.logback.classic.Logger;

@RestController
@RequestMapping("/v1/user")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private UserValidation userValidation;
	
//	@Autowired
//	org.slf4j.Logger logger = LoggerFactory.getLogger(UserController.class);

	@PostMapping("/register")
	public ResponseEntity<?> createUser(@RequestBody UserDTO user) {

		try {
			if (userValidation.userNullCheck(user)) {
				
				 logger.trace("A TRACE Message");
			        logger.debug("A DEBUG Message");
			        logger.info("An INFO Message");
			        logger.warn("A WARN Message");
			        logger.error("An ERROR Message");

				boolean isUserRegistered = userService.registerUser(user);
				if (isUserRegistered) {
					return new ResponseEntity<>(HttpStatus.CREATED);
				} else {
					return new ResponseEntity<>("Something went wrong while registering you..", HttpStatus.BAD_GATEWAY);
				}
			} else {
				return new ResponseEntity<>("Please fill all the field", HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {

			return new ResponseEntity<>("Something went wrong", HttpStatus.BAD_GATEWAY);
		}

	}

}
