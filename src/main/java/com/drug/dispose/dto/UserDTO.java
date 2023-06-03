package com.drug.dispose.dto;

import java.util.Date;

import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
public class UserDTO {
	
	private String name;
	private String primaryPhone;
	private String secondaryPhone;
	private String email;
	private Date dateOfBirth;
	private String gender;
	private String currentLocation;
	private String aadharNo;

}
