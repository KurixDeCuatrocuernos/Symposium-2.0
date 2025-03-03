package com.sympos2.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.sympos2.models.Usuario;
import com.sympos2.repositories.UserRepository;

/**
 * This service implements complex methods for ObraRepository.
 * @author KurixDeCuatroCuernos
 * @version 0.1.0
 */
@Service
public class UserService implements UserDetailsService{
	
	@Autowired
	private MongoTemplate mt; 
	
	@Autowired
	private UserRepository userRepo;
	
	private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
	/**
	 * This method allows to login by the email instead the user's name.
	 * @param email String with the email to load the user when login.
	 */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el email: " + email));
    }
	
    /**
     * This method edits an User in database (only works in the front-end without react).
     * @param edit Usuario with the new User data to save in database.
     * @return returns the User's data to edit. 
     */
	public Usuario edit(Usuario edit) {
		System.out.println("Se modificará: "+edit.getId()+"con nivel: "+edit.getRole());
		Optional<Usuario> user = userRepo.findById(edit.getId());
		
		if(user.isPresent()) {
			
			System.out.println("Contraseña recogida: "+edit.getPassword());
			if(edit.getPassword().isBlank()) {
				System.out.println("No se insertó contraseña, recuperando la anterior...");
				// En caso de no modificar la contraseña se deja la que tenía
				Optional<Usuario> userpass = userRepo.findById(edit.getId());
				edit.setPassword(userpass.get().getPassword());
			} else {
				System.out.println("Cambiando contraseña...");
				String pass = encoder.encode(edit.getPassword());
				edit.setPassword(pass);
				System.out.println("Se guardará la contraseña: "+pass);
			}
			userRepo.save(edit);
		} else {userRepo.save(edit);}
		
		return edit;
	}
}
