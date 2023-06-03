package com.drug.dispose.util;

import org.springframework.stereotype.Component;

import com.drug.dispose.dto.UserDTO;

@Component
public class UserValidation {

	public boolean userNullCheck(UserDTO user) {

		try {
			System.out.println("UserValidation :: In try block");

			if (user.getAadharNo() == null || user.getCurrentLocation() == null || user.getEmail() == null
					|| user.getName() == null || user.getPrimaryPhone() == null || user.getDateOfBirth() == null) {
				System.out.println("UserValidation :: null check for field");
				return false;

			}
			System.out.println("UserValidation :: All fields are non null");
			System.out.println("UserValidation :: " + user);
			return true;

		} catch (Exception e) {
			System.out.println("UserValidation :: Exception occured");
			throw new RuntimeException("Enter all the fields");
		}

	}

}
