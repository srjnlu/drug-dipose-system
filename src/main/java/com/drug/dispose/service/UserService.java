/**
 * 
 */
package com.drug.dispose.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.drug.dispose.dto.User;
import com.drug.dispose.entity.UserEntity;
import com.drug.dispose.repository.UserRespository;
import com.drug.dispose.util.UserHelper;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Abhishek_Singh6
 *
 */
@Service
@Slf4j
public class UserService {

	@Autowired
	private UserRespository userRespository;

	@Autowired
	private UserHelper userHelper;

	public User saveUser(final User user) {
		try {
			UserEntity entity = userHelper.mapUserDtoToEntity(user);
			log.info("User entity : {}", entity);
			userRespository.save(entity);

			log.error("User registered successfully");
			return user;
		} catch (Exception e) {
			log.error("Unable to register user!");
			return null;
		}

	}

}
