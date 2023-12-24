/**
 * 
 */
package com.drug.dispose.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.drug.dispose.entity.UserEntity;

/**
 * @author Abhishek_Singh6
 *
 */
@Repository
public interface UserRespository extends JpaRepository<UserEntity, Long> {
		
}
