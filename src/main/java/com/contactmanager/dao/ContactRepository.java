package com.contactmanager.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.contactmanager.entities.Contact;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
	
	//pegination...
	
	@Query("from Contact as c where c.user.id =:userId")
	//pegable ke pass two info hoga
	//current page
	//contact per page
	
	public Page<Contact> findContactsByUser(@Param("userId")int userId,Pageable pageable);

}
