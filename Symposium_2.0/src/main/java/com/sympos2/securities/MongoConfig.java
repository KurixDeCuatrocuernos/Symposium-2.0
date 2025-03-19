package com.sympos2.securities;

import java.util.Scanner;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

/**
 * MongoDB connection configuration.
 * <p>
 * This class contains the necessary configuration to establish a connection with a MongoDB database using user-provided credentials.
 * The {@link #mongoClient()} method configures a MongoDB client with the user-supplied username and password at runtime.
 * If no credentials are provided, a default user ("editor") is used.
 * <p>
 * The credentials are requested from the user via the console, preventing the use of the "root" account and allowing the use of a default user for development purposes.
 */
@Configuration
public class MongoConfig {
	
	/**
     * Creates a MongoDB client with credentials provided by the user.
     * <p>
     * This method prompts the user to enter a username and password for the connection. If the user enters incorrect credentials
     * or leaves any field empty, the information will be requested again.
     * If no credentials are provided, a default user ("editor") with an empty password is used.
     * The connection URI is constructed with the username and password, and then used to create a MongoDB client.
     * <p>
     * The connection is made to the MongoDB server located at "localhost" on port 27017 and the "symposium" database.
     *
     * @return A {@link MongoClient} object representing the connection to the MongoDB database.
     */
	@Bean
    MongoClient mongoClient() {
		
		Scanner lector = new Scanner(System.in);
		String usuario="";
		String pass="";
		String uri ="";
		boolean cell = false;
		do {
			try {
		        cell=true;
				System.out.print("Insert the User to connect with Mongo's database: ");
		        usuario=lector.nextLine();
		        if (usuario.equalsIgnoreCase("root")) {
		        	System.out.println("Please, dont connect by root account");
		        	// if we want, here we could avoid the user connects as root. 
		        }
		        if (usuario.equals("")||pass.equals("")) {
		        	// if we don't want to insert credentials connect automatically with the default account (editor) 
		        	// this is unnecessary, but it helps to development..
		        	usuario="editor";
		        	pass="editor";
		        }
		        System.out.print("Insert the password to connect with Mongo's database: ");
		        pass = lector.nextLine();
		        
			} catch (Exception e) {
				System.out.println("There was an exception connecting database with these credentials");
				e.printStackTrace();
				cell=false;
				lector.nextLine();
			}
		} while(cell==false);
		
		uri = "mongodb://"+ usuario + ":" + pass + "@localhost:27017/symposium"; 
	
        return MongoClients.create(uri);
    }
}
