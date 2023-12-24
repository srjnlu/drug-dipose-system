package com.drug.dispose.dto;

import java.time.LocalDateTime;

import com.drug.dispose.util.Gender;

import lombok.Data;

@Data
public class User {

	private String name;
	private String primaryPhone;
	private String secondaryPhone;
	private String email;
	private LocalDateTime dateOfBirth;
	private Gender gender;
	private String currentAddress;
	private String city;
	private String state;
	private String country;
	private String pinCode;
}
