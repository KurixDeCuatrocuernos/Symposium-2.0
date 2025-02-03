package com.sympos2.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

/**
 * This class generates a MongoTemplate to use the Service
 */
@Service
public class UserService {
	
	@Autowired
	private MongoTemplate mt; 
}
