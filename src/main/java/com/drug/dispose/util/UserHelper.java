package com.drug.dispose.util;

import org.springframework.stereotype.Component;

import com.drug.dispose.dto.User;
import com.drug.dispose.entity.AddressEntity;
import com.drug.dispose.entity.PhoneEntity;
import com.drug.dispose.entity.UserEntity;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UserHelper {

	public UserEntity mapUserDtoToEntity(final User user) {

		if (UserValidation.allMandatoryFieldsPresent(user)) {

			return UserEntity.builder().name(user.getName())
					.phone(PhoneEntity.builder().primaryPhone(user.getPrimaryPhone())
							.secondaryPhone(user.getSecondaryPhone()).build())
					.email(user.getEmail()).dateOfBirth(user.getDateOfBirth()).gender(user.getGender())
					.address(AddressEntity.builder().currentAddress(user.getCurrentAddress()).city(user.getCity())
							.state(user.getState()).pinCode(user.getPinCode()).country(user.getCountry()).build())
					.build();
		} else {
			log.error("All the required fields are not present {}", user);
			return null;
		}

	}

}
