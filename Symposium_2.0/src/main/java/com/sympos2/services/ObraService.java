package com.sympos2.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class ObraService {
	
	@Autowired 
	private MongoTemplate mt;
	
}
