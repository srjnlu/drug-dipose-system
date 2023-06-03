/**
 * 
 */
package com.drug.dispose.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.drug.dispose.dto.UserDTO;
import com.drug.dispose.entity.User;
import com.drug.dispose.repository.UserRespository;

/**
 * @author Abhishek_Singh6
 *
 */
@Service
public class UserService {

	@Autowired
	private UserRespository userRespository;

	//@Autowired
	//private User user;

	public boolean registerUser(UserDTO userDTO) {
		
		User user = new User();

		try {
			System.out.println("UserService :: UserDTO = "+userDTO);
			user.setName(userDTO.getName());
			user.setAadharNo(userDTO.getAadharNo());
			user.setCurrentLocation(userDTO.getCurrentLocation());
			user.setDateOfBirth(userDTO.getDateOfBirth());
			user.setEmail(userDTO.getEmail());
			user.setSecondaryPhone(userDTO.getSecondaryPhone());
			user.setPrimaryPhone(userDTO.getPrimaryPhone());
			user.setGender(userDTO.getGender());
			
			System.out.println("UserService :: User = "+user);
			
			userRespository.save(user);

			return true;

		} catch (Exception e) {
			throw new RuntimeException("Unable to register user");
		}

	}
}
