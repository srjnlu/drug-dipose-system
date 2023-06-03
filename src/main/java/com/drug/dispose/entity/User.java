package com.drug.dispose.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "user")
@Entity
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false)
	private String name;	
	@Column(name = "primary_phone", nullable = false)
	private String primaryPhone;
	@Column(name = "secondary_phone")
	private String secondaryPhone;
	@Column(nullable = false)
	private String email;
	@Column(name = "date_of_birth", nullable = false)
	private Date dateOfBirth;
	private String gender;
	@Column(name = "current_location", nullable = false)
	private String currentLocation;
	@Column(name = "aadhar", unique = true)
	private String aadharNo;

}
