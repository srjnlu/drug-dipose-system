package com.drug.dispose.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.drug.dispose.util.Gender;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "user")
@Entity
public class UserEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "name")
	private String name;
	
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "phone_id", referencedColumnName = "id")
	private PhoneEntity phone;	
	
	@Column(name = "email", nullable = false)
	private String email;
	
	@Column(name = "date_of_birth", nullable = false)
	private LocalDateTime dateOfBirth;
	
	@Column(name = "gender")
	private Gender gender;
	
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "address_id", referencedColumnName = "id")
	private AddressEntity address;
	
	
}
