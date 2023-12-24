package com.drug.dispose.util;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.drug.dispose.dto.User;

@Component
public class UserValidation {

	public static boolean allMandatoryFieldsPresent(final User user) {

		if(Objects.isNull(user.getName())) {
			return false;
		}
		if(Objects.isNull(user.getPrimaryPhone())) {
			return false;
		}
		if(Objects.isNull(user.getEmail())) {
			return false;
		}
		if(Objects.isNull(user.getCity())) {
			return false;
		}
		if(Objects.isNull(user.getCountry())) {
			return false;
		}
		if(Objects.isNull(user.getCurrentAddress())) {
			return false;
		}
		if(Objects.isNull(user.getState())) {
			return false;
		}
		return true;
		
	}

}
