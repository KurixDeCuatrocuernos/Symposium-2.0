package com.sympos2.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sympos2.dto.ComentarioPintado;
import com.sympos2.dto.ObraIsbnTituloProjection;
import com.sympos2.dto.UserRequestBody;
import com.sympos2.dto.UsuarioComentarioPintado;
import com.sympos2.models.Comentario;
import com.sympos2.models.Obra;
import com.sympos2.models.Usuario;
import com.sympos2.repositories.ComentarioRepository;
import com.sympos2.repositories.ObraRepository;
import com.sympos2.repositories.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * This Controller handles the redirections and logic of each front's URL, but the React's front-end templates.  
 * @author KurixDeCuatroCuernos
 * @version 0.1.0
 */
@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class MainRestController {
	
	@Autowired
	private ObraRepository obraRepo;
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private ComentarioRepository commentRepo;
	
	@Autowired
	private AuthenticationManager authManager;
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private BCryptPasswordEncoder encoder;
	
	/**
	 * This method handles the retrieval of the newest writing (latest work) from the database.
	 * 
	 * <p>
	 * It fetches the most recent work from the `obraRepo` sorted by its creation date in descending order.
	 * The result is then returned as a JSON response, which includes the work details if found,
	 * along with a status message indicating success or failure.
	 * </p>
	 *
	 * @return A ResponseEntity containing the status of the request and the newest writing in JSON format.
	 * @throws JsonProcessingException If there is an error during the JSON processing of the response.
	 */
	@GetMapping("/getNewestWriting")
	public ResponseEntity<String> getNewestWriting() throws JsonProcessingException{
		 Map<String, Object> rs = new HashMap<>();
		 ObjectMapper om = new ObjectMapper();
		 om.registerModule(new JavaTimeModule());  // Registra el módulo para LocalDateTime
		 om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		 
		 try {
			 List<Obra> obras = obraRepo.findOneByOrderByCreatedAtDesc();
			 System.out.println(obras.get(0));
			 
			 if (!obras.isEmpty()) {
				 rs.put("status", "true");
				 rs.put("writing", obras.get(0));
			 }
		 } catch (Exception e) {
			 rs.put("status", "false");
			 rs.put("message", "There was a exception in server: "+e);
		 }
		 
		 String json= om.writeValueAsString(rs);
		 return ResponseEntity.ok(json);
	}
	
	/**
	 * This method retrieves the most valued writing (work) based on the average rating from the database.
	 * 
	 * <p>
	 * It fetches all the works from the repository and calculates the average value for each work.
	 * The work with the highest average value is considered the most valued and is returned in the JSON response.
	 * In case of an error, an appropriate message is included in the response.
	 * </p>
	 *
	 * @return A ResponseEntity containing the status of the request and the most valued writing in JSON format.
	 * @throws JsonProcessingException If there is an error during the JSON processing of the response.
	 */
	@GetMapping("/getMostValuedWriting")
	public ResponseEntity<String> getMostValuedWriting() throws JsonProcessingException{
		 Map<String, Object> rs = new HashMap<>();
		 ObjectMapper om = new ObjectMapper();
		 om.registerModule(new JavaTimeModule());  // Registra el módulo para LocalDateTime
		 om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		 
		 try {
			 List<Obra> obras = obraRepo.findAll();
			 if (!obras.isEmpty()) {
				 Obra MVObra = new Obra();
				 int value=0;
				 for (Obra obra : obras) {
					 if (value < getAVG(obra.getIsbn())) {
						 MVObra=obra;
					 }
				 }
				 
				 rs.put("status", "true");
				 rs.put("writing", MVObra);
			 }
		 } catch (Exception e) {
			 rs.put("status", "false");
			 rs.put("message", "There was a exception in server: "+e);
		 }
		 
		 String json= om.writeValueAsString(rs);
		 return ResponseEntity.ok(json);
	}
	
	/**
	 * This method retrieves the most recent comment that is not from a "STUDENT" user and returns it as a response.
	 * 
	 * <p>
	 * The method first fetches all the comments of type "COMMENT" and all users with the role "STUDENT". 
	 * It then removes comments authored by "STUDENT" users from the list. Afterward, the method identifies the newest 
	 * comment (the one with the latest date) and returns it, along with the user's details (ID, name, and role) in the response.
	 * If any exception occurs during the process, an error message is included in the response.
	 * </p>
	 *
	 * @return A ResponseEntity containing the status of the request and the most recent comment (not by "STUDENT") in JSON format.
	 * @throws JsonProcessingException If there is an error during the JSON processing of the response.
	 */
	@GetMapping("/getTitledComment")
	public ResponseEntity<String> getTitledComment() throws JsonProcessingException{
		 Map<String, Object> rs = new HashMap<>();
		 ObjectMapper om = new ObjectMapper();
		 om.registerModule(new JavaTimeModule());  // Registra el módulo para LocalDateTime
		 om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		 
		 try {
			 
			 	List <Comentario> comments = commentRepo.findAllByTipo("COMMENT");
			 	List<Usuario> users = userRepo.findAllByRoleOnlyId("STUDENT");
		        // Crear un conjunto de IDs de usuarios con rol "STUDENT"
		        Set<String> studentIds = users.stream()
		                                      .map(Usuario::getId) // Obtener los IDs de los usuarios
		                                      .collect(Collectors.toSet()); // Guardarlos en un Set para una búsqueda eficiente

		        // Usamos un Iterator para eliminar los comentarios de estudiantes
		        Iterator<Comentario> iterator = comments.iterator();
		        while (iterator.hasNext()) {
		            Comentario comment = iterator.next();
		            if (studentIds.contains(comment.getUsuario())) {
		                iterator.remove(); // Elimina el comentario si su usuario es estudiante
		            }
		        }
			    Optional<Comentario> newestComment = comments.stream().max((c1, c2) -> c1.getFecha().compareTo(c2.getFecha()));
			 	Optional<UsuarioComentarioPintado> user = userRepo.findByIdOnlyIdAndNameAndRole(newestComment.get().getUsuario());
			 	ComentarioPintado commentToSend = new ComentarioPintado(newestComment.get(), user.get().id(), user.get().name(), user.get().role());
			 	rs.put("comment", commentToSend);
			 	rs.put("status", "true");
				
		 } catch (Exception e) {
			 rs.put("status", "false");
			 rs.put("message", "There was a exception in server: "+e);
		 }
		 
		 String json= om.writeValueAsString(rs);
		 return ResponseEntity.ok(json);
	}
	
	/**
	 * This method bans a comment or an answer by changing its type to "BANNED_COMMENT" or "BANNED_ANSWER", respectively.
	 * It receives the ID of the comment or answer, finds the corresponding entity, and updates its type to mark it as banned.
	 * 
	 * <p>
	 * If the comment or answer is found, the method changes its type to "BANNED_COMMENT" for comments or "BANNED_ANSWER" 
	 * for answers and saves the updated entity. If the provided ID does not correspond to a valid comment or answer, 
	 * or if there is an error during the operation, an appropriate error message is included in the response.
	 * </p>
	 *
	 * @param id The ID of the comment or answer to be banned.
	 * @return A ResponseEntity containing the status of the request and a message, in JSON format.
	 * @throws JsonProcessingException If there is an error during the JSON processing of the response.
	 */
	@GetMapping("/getBanComment")
	public ResponseEntity<String> getBanComment(@RequestParam String id) throws JsonProcessingException {
		Map<String, Object> rs = new HashMap<>();
		ObjectMapper om = new ObjectMapper();
		
		if (id != null) {
			try {
				Optional<Comentario> comment = commentRepo.findById(id);
				if (comment.isPresent()) {
					if(comment.get().getTipo().equals("COMMENT")) {
						comment.get().setTipo("BANNED_COMMENT");
						commentRepo.save(comment.get());
						rs.put("status", "true");
					} else if (comment.get().getTipo().equals("ANSWER")) {
						comment.get().setTipo("BANNED_ANSWER");
						commentRepo.save(comment.get());
						rs.put("status", "true");
					} else {
						rs.put("status", "false");
						rs.put("message", "The comment to delete is not a Cmment nor an Answer");
					}
						
				} else {
					rs.put("status", "false");
					rs.put("message", "Couldn't find a comment with id: "+id);
				}
				
			} catch (Exception e) {
				rs.put("status", "false");
				rs.put("message", "There was a exception in server: "+e);
			}
		} else {
			rs.put("status", "false");
			rs.put("message", "Server didn't recieve Id");
		}
		
		
		String json= om.writeValueAsString(rs);
		return ResponseEntity.ok(json);
	}
	
	/**
	 * This method retrieves all banned comments and answers from the database and returns them in a response.
	 * It fetches comments with type "BANNED_COMMENT" and answers with type "BANNED_ANSWER". For each banned comment 
	 * or answer, it retrieves the associated user details and creates a response object that includes the comment/answer 
	 * along with user information (ID, username, and role).
	 * 
	 * <p>
	 * If there are any banned comments or answers, the method constructs an array of `ComentarioPintado` objects, 
	 * which contain both the comment and its associated user details. If no banned comments or answers are found, 
	 * the response will indicate "false".
	 * </p>
	 *
	 * @return A ResponseEntity containing a JSON object with the status of the request and the list of banned comments 
	 *         or answers, or an error message if an exception occurs during the process.
	 * @throws JsonProcessingException If there is an error during the JSON processing of the response.
	 */
	@GetMapping("/getAllBanComments")
	public ResponseEntity<String> getAllBanComments() throws JsonProcessingException{
		Map<String, Object> rs = new HashMap<>();
		ObjectMapper om = new ObjectMapper();
		om.registerModule(new JavaTimeModule());  // Registra el módulo para LocalDateTime
		om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		 
		try {
			List<Comentario> banComments = commentRepo.findAllByTipo("BANNED_COMMENT");
			List<Comentario> banAnswers = commentRepo.findAllByTipo("BANNED_ANSWER");
			List<ComentarioPintado> comments = new ArrayList<ComentarioPintado>();
			for (Comentario comment : banComments) {
				Optional<Usuario> user = userRepo.findById(comment.getUsuario());
				if (user.isPresent()) {
					comments.add(new ComentarioPintado(comment, user.get().getId(), user.get().getUsername(), user.get().getRole()));
				}
			}
			for (Comentario comment : banAnswers) {
				Optional<Usuario> user = userRepo.findById(comment.getUsuario());
				if (user.isPresent()) {
					comments.add(new ComentarioPintado(comment, user.get().getId(), user.get().getUsername(), user.get().getRole()));
				}
			}
			if (!comments.isEmpty()) {
				System.out.println("Banned Comments sended");
				rs.put("status", "true");
				rs.put("array", comments);
			} else {
				System.out.println("No Banned Comments found");
				rs.put("status", "true");
				rs.put("array", "false");
			}
		} catch (Exception e) {
			rs.put("status", "false");
			rs.put("message", "There was a exception in server: "+e);
		}
		
		String json= om.writeValueAsString(rs);
		return ResponseEntity.ok(json);
	}
	
	/**
	 * This method deletes a comment (and its associated answers, if any) from the database.
	 * It first checks if the provided comment ID has any associated answers. If answers exist,
	 * those are also deleted before deleting the original comment.
	 * 
	 * <p>
	 * The process works as follows:
	 * - If the provided comment ID is not null, the method looks for all answers related to the comment.
	 * - If answers exist, they are deleted from the database.
	 * - Afterward, the original comment is deleted as well.
	 * </p>
	 *
	 * <p>
	 * The response will indicate the success or failure of the deletion process. If an exception is thrown,
	 * it is caught and an error message is returned. If no ID is provided, a message indicating that the ID 
	 * was not received will be returned.
	 * </p>
	 *
	 * @param id The ID of the comment to be deleted.
	 * @return A ResponseEntity containing a JSON object with the status of the deletion operation.
	 *         If the operation is successful, it will return a status of "true". If an error occurs,
	 *         it will return a status of "false" with an error message.
	 * @throws JsonProcessingException If there is an error during the JSON processing of the response.
	 */
	@GetMapping("/getDelComment")
	public ResponseEntity<String> getDeleteComment(@RequestParam String id) throws JsonProcessingException {
		Map<String, Object> rs = new HashMap<>();
		ObjectMapper om = new ObjectMapper();
		
			if (id != null) {
				try {
					
					List<Comentario> answers = commentRepo.findAllByComment(id);
					if (!answers.isEmpty()) {
						for (Comentario answer : answers) {
							commentRepo.deleteById(answer.getId());
						}
					}
					commentRepo.deleteById(id);
					rs.put("status", "true");
					
				} catch (Exception e) {
					rs.put("status", "false");
					rs.put("message", "There was a exception in server: "+e);
				}
			} else {
				rs.put("status", "false");
				rs.put("message", "Server didn't recieve Id");
			}
		
		String json= om.writeValueAsString(rs);
		return ResponseEntity.ok(json);
	}
	
	/**
	 * This method unbans a previously banned comment or answer by changing its type back to "COMMENT" or "ANSWER".
	 * 
	 * <p>
	 * The process works as follows:
	 * - If the provided comment ID is not null, the method tries to find the comment with the given ID.
	 * - If the comment is found and it is banned (i.e., its type is "BANNED_COMMENT" or "BANNED_ANSWER"), the method changes its type back to "COMMENT" or "ANSWER" respectively.
	 * - If the comment is not banned or does not exist, an appropriate message is returned in the response.
	 * </p>
	 *
	 * <p>
	 * The response indicates whether the unban operation was successful or not. If an exception occurs during the process,
	 * it is captured and returned as an error message. If no ID is provided, a message indicating that the ID was not received 
	 * will be returned.
	 * </p>
	 *
	 * @param id The ID of the comment or answer to be unbanned.
	 * @return A ResponseEntity containing a JSON object with the status of the unban operation.
	 *         If the operation is successful, it will return a status of "true". If an error occurs,
	 *         it will return a status of "false" with an error message.
	 * @throws JsonProcessingException If there is an error during the JSON processing of the response.
	 */
	@GetMapping("/getUnbanComment")
	public ResponseEntity<String> getUnbanComment(@RequestParam String id) throws JsonProcessingException {
		Map<String, Object> rs = new HashMap<>();
		ObjectMapper om = new ObjectMapper();
		
			if (id != null) {
				try {
					Optional<Comentario> comment = commentRepo.findById(id);
					
					if (comment.isPresent()) {
						if(comment.get().getTipo().equals("BANNED_COMMENT")) {
								comment.get().setTipo("COMMENT");
								commentRepo.save(comment.get());
								rs.put("status", "true");
							} else if (comment.get().getTipo().equals("BANNED_ANSWER")) {
								comment.get().setTipo("ANSWER");
								commentRepo.save(comment.get());
								rs.put("status", "true");
							} else {
								rs.put("status", "false");
								rs.put("message", "The comment to unban was not a Comment nor an Answer");
							}


					} else {
						rs.put("status", "false");
						rs.put("message", "Couldn't find a comment with id: "+id);
					}

					
				} catch (Exception e) {
					rs.put("status", "false");
					rs.put("message", "There was a exception in server: "+e);
				}
			} else {
				rs.put("status", "false");
				rs.put("message", "Server didn't recieve Id");
			}
		
		String json= om.writeValueAsString(rs);
		return ResponseEntity.ok(json);
	}
	
	/**
	 * This method retrieves the most recent comment made by a user with the role "TITLED", 
	 * excluding comments made by students, and returns it along with the user's information.
	 * 
	 * <p>
	 * The process works as follows:
	 * - The method fetches all comments of type "COMMENT" from the database.
	 * - It then fetches a list of users with the role "TITLED" (not students).
	 * - The comments made by users with the "STUDENT" role are removed from the list.
	 * - The method selects the most recent comment from the remaining comments and fetches the user's information associated with it.
	 * - It returns the comment along with the user's ID, name, and role in the response.
	 * </p>
	 *
	 * <p>
	 * If an error occurs during the process, an appropriate error message is included in the response.
	 * If no valid comment is found, a message indicating the failure is returned. 
	 * </p>
	 *
	 * @return A ResponseEntity containing a JSON object with the status of the operation and the most recent comment made by a "TITLED" user.
	 *         If the operation is successful, the comment, along with the user's details, is included in the response. If there is an error,
	 *         a status of "false" and an error message will be returned.
	 * @throws JsonProcessingException If there is an error during the JSON processing of the response.
	 */
	@GetMapping("/getStudentComment")
	public ResponseEntity<String> getStudentComment() throws JsonProcessingException{
		 Map<String, Object> rs = new HashMap<>();
		 ObjectMapper om = new ObjectMapper();
		 om.registerModule(new JavaTimeModule());  // Registra el módulo para LocalDateTime
		 om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		 
		 try {
			 
			 	List <Comentario> comments = commentRepo.findAllByTipo("COMMENT");
			 	List<Usuario> users = userRepo.findAllByRoleOnlyId("TITLED");
		        // Crear un conjunto de IDs de usuarios con rol "STUDENT"
		        Set<String> studentIds = users.stream()
		                                      .map(Usuario::getId) // Obtener los IDs de los usuarios
		                                      .collect(Collectors.toSet()); // Guardarlos en un Set para una búsqueda eficiente

		        // Usamos un Iterator para eliminar los comentarios de estudiantes
		        Iterator<Comentario> iterator = comments.iterator();
		        while (iterator.hasNext()) {
		            Comentario comment = iterator.next();
		            if (studentIds.contains(comment.getUsuario())) {
		                iterator.remove(); // Elimina el comentario si su usuario es estudiante
		            }
		        }
			   
		        Optional<Comentario> newestComment = comments.stream().max((c1, c2) -> c1.getFecha().compareTo(c2.getFecha()));
			 	Optional<UsuarioComentarioPintado> user = userRepo.findByIdOnlyIdAndNameAndRole(newestComment.get().getUsuario());
			 	ComentarioPintado commentToSend = new ComentarioPintado(newestComment.get(), user.get().id(), user.get().name(), user.get().role());
			 	
			 	rs.put("comment", commentToSend);
			 	rs.put("status", "true");
				
		 } catch (Exception e) {
			 rs.put("status", "false");
			 rs.put("message", "There was a exception in server: "+e);
		 }
		 
		 String json= om.writeValueAsString(rs);
		 return ResponseEntity.ok(json);
	}
	
	/**
	 * This method retrieves a list of book suggestions based on the provided search term. 
	 * If no search term is provided, it returns all available book suggestions from the repository.
	 * 
	 * <p>
	 * The method fetches a list of books with their ISBN and title from the repository, and if the list is not empty, 
	 * it returns the suggestions. If the list is empty, a message is logged indicating that suggestions could not be fetched.
	 * </p>
	 *
	 * @param searchTerm An optional search term to filter the book suggestions. If no term is provided, all books are returned.
	 * 
	 * @return A list of {@link ObraIsbnTituloProjection} containing the ISBN and title of the books. 
	 *         If no suggestions are found, an empty list is returned.
	 */
	@GetMapping("/sugerencias")
    public List<ObraIsbnTituloProjection> suggestList(@RequestParam(required = false) String searchTerm) {
        List<ObraIsbnTituloProjection> retorno = new ArrayList<ObraIsbnTituloProjection>();
        List<ObraIsbnTituloProjection> suggestions = obraRepo.findAllIsbnAndTitulo();
        
        if (!suggestions.isEmpty()) {
        	
            retorno = suggestions;
            
        } else {
            System.out.println("No se pudo recoger las sugerencias");
        }
        return retorno;
    }
	
	/**
	 * Retrieves the role of the currently authenticated user.
	 * 
	 * <p>
	 * This method checks if the user is authenticated and if their session is not from an anonymous user.
	 * If the user is authenticated, it fetches the user's role from the database and returns it as a JSON response.
	 * If the user is not found or there is an error during the process, it returns a JSON response indicating the failure.
	 * </p>
	 *
	 * @return A {@link ResponseEntity} containing a JSON response with the user's role. If the user is authenticated,
	 *         the role is included in the response, otherwise a response with "role": null is returned.
	 *         In case of an error during serialization, a 500 error with a specific error message is returned.
	 */
	@GetMapping("/getUserRole")
	public ResponseEntity<String> getUserRole() {
		System.out.println("Enviando Role del usuario");
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

	    if (auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
	        try {
	        	Optional<Usuario> user = userRepo.findByEmail(auth.getName());
	        	
	        	if (user.isPresent()) {
	        		System.out.println("role: "+user.get().getRole());
	        		ObjectMapper toJson = new ObjectMapper();
	        		String json = toJson.writeValueAsString(user.get().getRole());
		            System.out.println("Json recogido: " + json);
		            return ResponseEntity.ok(json);
	        	} else {
	        		return ResponseEntity.ok("{\"role\": null}"); 
	        	}
	        } catch (Exception e) {
	            e.printStackTrace();
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body("{\"error\": \"Error serializando el JSON\"}");
	        }
	    } else {
	        return ResponseEntity.ok("{\"role\": null}"); // JSON válido en vez de "null"
	    }
	}

	/**
	 * Retrieves the avatar and username of the currently authenticated user.
	 * 
	 * <p>
	 * This method checks if the user is authenticated. If the user is authenticated and not anonymous, it fetches
	 * the user's details (including avatar and username) from the database. The avatar and username are returned
	 * as a JSON response. If the user does not have an avatar, a default avatar image URL is provided.
	 * If any error occurs during the process or if the user is not authenticated, an appropriate error message is returned.
	 * </p>
	 *
	 * @return A {@link ResponseEntity} containing a JSON response with the user's avatar and username.
	 *         If the user is not authenticated, a bad request response is returned with an error message.
	 *         If an error occurs while processing the request, a server error response is returned.
	 */
	@GetMapping("/getUserAvatar")
	public ResponseEntity<String> getUserAvatar() {
	    System.out.println("Se ha buscado al usuario");
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

	    if (auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
	        try {
	            System.out.println("Buscando al usuario en BD: " + auth.getName());
	            Optional<Usuario> user = userRepo.findByEmailOnlyId(auth.getName());

	            if (user.isPresent()) {
	                Optional<UsuarioComentarioPintado> userToSend = userRepo.findByIdOnlyIdAndNameAndRole(user.get().getId());

	                if (userToSend.isPresent()) {
	                    UsuarioComentarioPintado usuario = userToSend.get();
	                    
	                    // Crear JSON estructurado
	                    Map<String, String> responseData = new HashMap<>();
	                    responseData.put("avatar", usuario.avatar() != null ? usuario.avatar() : "./usuario.png");
	                    responseData.put("username", usuario.name() != null ? usuario.name() : "Usuario desconocido");

	                    // Serializar correctamente el objeto como JSON
	                    ObjectMapper toJson = new ObjectMapper();
	                    String json = toJson.writeValueAsString(responseData);
	                    
	                    return ResponseEntity.ok(json);
	                }
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            return ResponseEntity.internalServerError().body("{\"error\": \"Error interno del servidor\"}");
	        }
	    }
	    
	    return ResponseEntity.badRequest().body("{\"error\": \"Usuario no autenticado\"}");
	}
	
	/**
	 * Retrieves the username of the currently authenticated user.
	 * 
	 * <p>
	 * This method checks if the user is authenticated. If the user is authenticated and not anonymous, it attempts
	 * to fetch the user's details (name) from the database. If the user exists, the name is returned. If the user is 
	 * not found in the database, it returns the username from the authentication token. If the user is not authenticated,
	 * the response will indicate that the user is not authenticated.
	 * </p>
	 *
	 * @return A {@link ResponseEntity} containing a JSON response with the status and the username of the authenticated user.
	 *         If the user is not authenticated, the response will indicate that the user is not authenticated.
	 *         The response includes a status key and a user key that contains the username or a "false" value if the user is not authenticated.
	 * @throws JsonProcessingException if an error occurs while serializing the response to JSON.
	 */
	@GetMapping("/getUsername")
	public ResponseEntity<String> getUsername() throws JsonProcessingException{
		Map<String, Object> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    if (auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
	    	
	    	Optional<Usuario> user = userRepo.findByEmail(auth.getName());
	    	if(user.isPresent()) {
	    		rs.put("status", "true");
	    		rs.put("user", user.get().getName());
	    	} else {
	    		rs.put("status", "true");
	    		rs.put("user", auth.getName());
	    	}
	    	
	    	
	    } else {
	    	rs.put("status", "true");
	    	rs.put("user", "false");
	    }
	    
	    String json = om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}

	/**
	 * Handles user login by authenticating the provided credentials (email and password).
	 * 
	 * <p>
	 * This method takes a {@link UserRequestBody} object, which contains the user's email and password. It then attempts to
	 * authenticate the user by checking the credentials against the database. If the credentials are correct, a session is
	 * created, and the user is authenticated. If the credentials are incorrect or if an error occurs, an appropriate
	 * message is returned in the response.
	 * </p>
	 *
	 * @param user A {@link UserRequestBody} object containing the email and password of the user attempting to log in.
	 * @return A {@link ResponseEntity} containing a JSON response with the login status:
	 *         - If authentication is successful, it returns a status of "true".
	 *         - If authentication fails (wrong email/password), it returns a status of "false" and an error message.
	 *         - If an internal error occurs, it returns a status of "false" and an error message indicating the problem.
	 * @throws JsonProcessingException If there is an error serializing the response to JSON.
	 */
	@PostMapping("/getLogin")
	public ResponseEntity<String> loginUser(@RequestBody UserRequestBody user) throws JsonProcessingException {
	    System.out.println("Tratando de iniciar sesión: " + user.email());
	    Map<String, String> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    
	    if (user != null) {
	        try {
	            Optional<Usuario> userLogged = userRepo.findByEmail(user.email());
	            if (userLogged.isPresent()) {
	            	try {
		            	UsernamePasswordAuthenticationToken authToken =
		                     new UsernamePasswordAuthenticationToken(userLogged.get().getEmail(), user.password());
		                System.out.println("Usuario encontrado en BD: " + userLogged.get().getEmail());
		                Authentication authentication = authManager.authenticate(authToken);
		                SecurityContextHolder.getContext().setAuthentication(authentication);
	
		                // Guardar en sesión HTTP
		                HttpSession session = request.getSession(true);
		                session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
						rs.put("status", "true");
		                System.out.println("Sesión iniciada correctamente");
	            	} catch (BadCredentialsException excec) {
	            		rs.put("status", "false");
	 	                rs.put("message", "These credentials don't match, please check the password and email, and look caps and numbers");
	            	}
	               
	                

	                
	            } else {
	                rs.put("status", "false");
	                rs.put("message", "These credentials don't match, please check the password and email, and look caps and numbers");
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            rs.put("status", "false");
	            rs.put("message", "Internal server error, try it later or contact with an Admin");
	        }
	    }
	    String json = om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}

	/**
	 * Logs out the current authenticated user by invalidating the HTTP session and clearing the security context.
	 * 
	 * <p>
	 * This method invalidates the user's session and clears the security context, effectively logging the user out of the application. 
	 * It handles any internal errors during the logout process by returning an error message. If the logout is successful, 
	 * it returns a confirmation message.
	 * </p>
	 *
	 * @param request The {@link HttpServletRequest} object used to retrieve the current session.
	 * @param response The {@link HttpServletResponse} object, which is not used directly in this method but is included as part of the signature.
	 * @return A {@link ResponseEntity} containing a string message indicating the result of the logout attempt:
	 *         - "Logout exitoso" if the user has been logged out successfully.
	 *         - "Error al cerrar sesión" if there is an error during the logout process.
	 */
	@PostMapping("/getLogout")
	public ResponseEntity<String> logoutUser(HttpServletRequest request, HttpServletResponse response) {
	    try {
	        // Invalidar la sesión
	        HttpSession session = request.getSession(false);
	        if (session != null) {
	            session.invalidate();
	        }

	        // Eliminar la autenticación del contexto de seguridad
	        SecurityContextHolder.clearContext();

	        return ResponseEntity.ok("Logout exitoso");
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.internalServerError().body("Error al cerrar sesión");
	    }
	}
	
	/**
	 * Retrieves a work (Obra) by its ID and returns its details in a JSON format.
	 * 
	 * <p>
	 * This method takes the ID of a work and attempts to retrieve its corresponding data from the database. 
	 * It returns various details about the work, including its title, author, publication date, place of publication, 
	 * publisher, type, abstract, and average rating. If the work is an article, the page range is also included. 
	 * The response is formatted as a JSON object containing the work's data or an error message if the work is not found.
	 * </p>
	 *
	 * @param id The ID of the work (Obra) to be retrieved.
	 * @return A {@link ResponseEntity} containing a JSON object with the following:
	 *         - If the work is found, the work's details, including title, author, publication date, and other relevant fields.
	 *         - If the work is not found, a message indicating that the work with the given ID was not found.
	 *         - Error messages in case of issues during the retrieval process or if the ID is not provided.
	 */
	@GetMapping("/getWriting")
	public ResponseEntity<String> getWriting(@RequestParam Long id) throws JsonProcessingException {
	    Map<String, String> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    
	    System.out.println("Buscando obra con isbn: " + id);

	    if (id != null) {
	        try {
	            Optional<Obra> obra = obraRepo.findById(id);
	            if (obra.isPresent()) {
	                int valorMedia = getAVG(id);
	           
	                rs.put("titulo", obra.get().getTitulo());
	                rs.put("autor", obra.get().getAutor());
	                rs.put("fechaPub", obra.get().getFechaPublicacion().toString());
	                rs.put("place", obra.get().getLugar_publicacion());
	                rs.put("edit", obra.get().getEditorial());
	                rs.put("type", obra.get().getTipo());
	                rs.put("abstract", obra.get().getAbstracto());
	                rs.put("valoracion", String.valueOf(valorMedia));
	                
	                if (!obra.get().getTemas().isEmpty()) {
	                    rs.put("temas", obra.get().getTemas().toString());
	                }

	                if (obra.get().getTipo().equals("ARTICLE")) {
	                    rs.put("paginaIni", String.valueOf(obra.get().getPaginaini()));
	                    rs.put("paginaFin", String.valueOf(obra.get().getPaginafin()));
	                }
	                
	                rs.put("status", "true");
	                rs.put("message", "Obra encontrada con éxito");	                	                
	            } else {
	                rs.put("status", "false");
	                rs.put("message", "No se ha encontrado obra con esa id");
	            }
	        } catch (Exception e) {
	        	
	            e.printStackTrace();
	            rs.put("status", "false");
	            rs.put("message", "Error al buscar la obra con esa id");
	        }
	    } else {
	        rs.put("status", "false");
	        rs.put("message", "No se ha recibido id para buscar");
	    }
	    String json = om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}

	/**
	 * Retrieves comments for a specific work (Obra) filtered by user role and ordered by date.
	 * 
	 * <p>
	 * This method takes an ID of a work (Obra) and a user role as parameters. It fetches all comments 
	 * associated with the given work, filtering them based on the role of the user who made the comment. 
	 * The comments are sorted in descending order by their date of creation. If comments are found, 
	 * it returns a JSON response with the details of each comment, including the title, text, date, rating, 
	 * username, school, and role of the user who posted the comment. 
	 * If no comments are found or an error occurs, appropriate error messages are returned.
	 * </p>
	 *
	 * @param id The ID of the work (Obra) whose comments are to be retrieved.
	 * @param role The role of the user whose comments are to be filtered (e.g., "STUDENT", "TITLED").
	 * @return A {@link ResponseEntity} containing a JSON object with the following:
	 *         - If comments are found, a list of comments including their details (title, text, date, etc.).
	 *         - If no comments are found, a message indicating that no comments exist for the given work.
	 *         - Error messages if issues occur during data retrieval or processing.
	 */
	@GetMapping("/getComentarios")
	public ResponseEntity<String> getComentarios(@RequestParam Long id, @RequestParam String role) throws JsonProcessingException {
	    System.out.println("Recogiendo los comentarios de la obra");
	    System.out.println("isbn: "+id);
	    Map<String, Object> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();

	    try {
	        Sort sortByDate = Sort.by(Order.desc("fecha"));
	        List<Comentario> comments = commentRepo.findAllByObraAndTipo(id, "COMMENT", sortByDate);
	        System.out.println("comentarios: "+comments);
	        if (!comments.isEmpty()) {
	            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	            List<Map<String, String>> list = new ArrayList<>();

	            for (Comentario comment : comments) {
	                Optional<UsuarioComentarioPintado> userData = userRepo.findByIdOnlyIdAndNameAndRole(comment.getUsuario());
	                
	                if (userData.isPresent() && userData.get().role().equals(role)) {
	                    Map<String, String> rd = new HashMap<>();
	                    rd.put("title", comment.getTitulo());
	                    rd.put("text", comment.getTexto());
	                    rd.put("id", comment.getId());
	                    rd.put("datetime", comment.getFecha().format(formatter));
	                    rd.put("value", String.valueOf(comment.getValoracion()));
	                    rd.put("username", userData.get().name());
	                    rd.put("school", userData.get().school());
	                    rd.put("role", userData.get().role());
	                    list.add(rd);
	                }
	            }
	            
	            // Creando la respuesta JSON
	            rs.put("status", "true");
	            rs.put("comments", list);
	            rs.put("message", "Comentarios encontrados con éxito");

	        } else {
	            rs.put("status", "false");
	            rs.put("message", "No se encontraron comentarios para esta obra.");	            
	        }
	    } catch (Exception e) {
	        rs.put("status", "false");
	        rs.put("message", "Error al obtener los comentarios: " + e.getMessage());
	    }

	    String json=om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}

	/**
	 * Retrieves all answers associated with a specific comment for a particular work (Obra).
	 * 
	 * <p>
	 * This method takes the ID of a work (Obra) and a specific comment ID as parameters. It fetches all responses (answers) 
	 * to the given comment for the specified work, sorted in descending order by the date they were created. The responses 
	 * include information such as the title, text, date, rating, username, school, role of the user who posted the answer, 
	 * and the comment they are answering. If no answers are found or if an error occurs, an appropriate response is returned.
	 * </p>
	 *
	 * @param id The ID of the work (Obra) for which answers are being retrieved.
	 * @param comment The ID of the comment to which the answers belong.
	 * @return A {@link ResponseEntity} containing a JSON object with:
	 *         - An array of answers with their details (title, text, date, rating, username, school, role, and original comment).
	 *         - If no answers are found, an empty array is returned.
	 *         - Error messages if any issues arise during data retrieval or processing.
	 */
	@GetMapping("/getAnswers")
	public ResponseEntity<String> getAnswers(@RequestParam Long id, @RequestParam String comment) throws JsonProcessingException {
	    Map<String, Object> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    om.registerModule(new JavaTimeModule());  // Registra el módulo para LocalDateTime
	    om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	    
	    try {
	        // Ordenamos por fecha descendente
	        Sort sortByDate = Sort.by(Order.desc("fecha"));
	        
	        // Obtenemos las respuestas asociadas al comentario y la obra
	        List<Comentario> answers = commentRepo.findAllByObraAndTipoAndComment(id, "ANSWER", comment, sortByDate);
	        
	        if (answers != null && !answers.isEmpty()) {
	            // Lista para almacenar las respuestas formateadas
	            List<Map<String, String>> list = new ArrayList<>();
	            
	            // Recorremos cada respuesta
	            for (Comentario answer : answers) {
	                // Obtenemos los datos del usuario que realizó la respuesta
	                Optional<UsuarioComentarioPintado> userData = userRepo.findByIdOnlyIdAndNameAndRole(answer.getUsuario());
	                
	                if (userData.isPresent()) {
	                    // Creamos un mapa para la respuesta con la información formateada
	                    Map<String, String> rd = new HashMap<>();
	                    rd.put("title", answer.getTitulo());
	                    rd.put("text", answer.getTexto());
	                    rd.put("id", answer.getId());
	                    rd.put("datetime", answer.getFecha().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
	                    rd.put("value", String.valueOf(answer.getValoracion()));
	                    rd.put("username", userData.get().name());
	                    rd.put("school", userData.get().school());
	                    rd.put("role", userData.get().role());
	                    rd.put("comentario", answer.getComment());
	                    
	                    // Añadimos la respuesta formateada a la lista
	                    list.add(rd);
	                }
	            }
	           
	            rs.put("array", list);
	            System.out.println(list.toString());
	        } else {

	            rs.put("array", new ArrayList<>());
	        }
	        
	        // Añadimos la información del estado y el mensaje
	        rs.put("status", "true");
	        rs.put("message", "Comentarios encontrados con éxito");
	    } catch (Exception e) {
	        // Si ocurre un error, asignamos el estado "false"
	        rs.put("status", "false");
	        rs.put("message", "Error al obtener los comentarios: " + e.getMessage());
	    }

	    // Convertimos el mapa a un JSON
	    String json = om.writeValueAsString(rs);
	    
	    // Devolvemos la respuesta como JSON
	    return ResponseEntity.ok(json);
	}
	
	/**
	 * Checks whether a specific user has commented on a particular work (Obra).
	 *
	 * <p>
	 * This method searches for comments made by the specified user for a given work (Obra). It checks if any of the 
	 * comments made by the user are of type "COMMENT". If a comment of this type is found, the response will indicate 
	 * that the user has commented on the work, otherwise, it will indicate that the user has not commented. The response 
	 * is returned as a JSON object containing a single key-value pair: "commented" with a value of "true" or "false".
	 * </p>
	 *
	 * @param id The ID of the work (Obra) to search for comments.
	 * @param user The username of the user whose comments are being checked.
	 * @return A {@link ResponseEntity} containing a JSON object with the key "commented" and a value of either "true" 
	 *         or "false", indicating whether the user has commented on the work. If an error occurs during the operation, 
	 *         a 500 error with an appropriate message is returned.
	 */
	@GetMapping("/getCommented")
	public ResponseEntity<String> getCommented(Long id, String user) {
		System.out.println("Se va a buscar algún comentario del usuario: "+user+" en la obra : "+id);
		ResponseEntity<String> response = ResponseEntity.ok("{\"commented\": \"false\"}");
		Map <String, String> rd = new HashMap<>();
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			List<Comentario> comments = commentRepo.findAllByObraAndUsuario(id, user);
			if (!comments.isEmpty()) {
				boolean commented = false;
				for (Comentario comment : comments) {
					if (comment.getTipo().equals("COMMENT")) {
						commented = true;
						System.out.println("Has comentado");
					}
				}
				rd.put("commented", String.valueOf(commented));
				String json = objectMapper.writeValueAsString(rd);
				response = ResponseEntity.ok(json);
			}
			
		} catch (Exception e) {
			response = ResponseEntity.internalServerError().body("Cannot get comments of user");
		}
		
		
		return response;
	}
	
	/**
	 * Retrieves the ID of the authenticated user.
	 *
	 * <p>
	 * This method checks if the current user is authenticated. If the user is authenticated, it fetches the user's 
	 * ID from the database based on their email (retrieved from the authentication context). If successful, the user's 
	 * ID is returned in the response. If the user is not authenticated or an error occurs while fetching the user data, 
	 * an appropriate error message is returned.
	 * </p>
	 *
	 * @return A {@link ResponseEntity} containing a JSON object with the user's ID and status:
	 *         - If the user is authenticated and their ID is found, the response will contain:
	 *           - `"status": "true"`, `"message": "User found"`, and `"id": "<user_id>"`.
	 *         - If the user is not authenticated or there is an error fetching the user, the response will contain:
	 *           - `"status": "false"` and an appropriate error message.
	 */
	@GetMapping("/getUserIdent")
	public ResponseEntity<String> getUserId() throws JsonProcessingException {
	    Map<String, String> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();	    
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    
	    if (auth == null || auth.getName() == null || auth.getName().equals("anonymousUser")) {
	        rs.put("status", "false");
	        rs.put("message", "User not authenticated");	       
	    } else {
	        String id = null;
	        try {
	            System.out.println("Buscando al usuario con email: " + auth.getName());
	            Optional<Usuario> user = userRepo.findByEmailOnlyId(auth.getName());
	            System.out.println("Usuario: "+user.toString());
	            if (user.isPresent()) {
	                id = user.get().getId();
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            rs.put("status", "false");
	            rs.put("message", "Error fetching user");
	        }

	        if (id != null) {
	            rs.put("status", "true");
	            rs.put("message", "User found");
	            rs.put("id", id);
	            
	        } else {
	            rs.put("status", "false");
	            rs.put("message", "Cannot get User Identity");
	        }
	    }
	    String json = om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}

	/**
	 * Inserts a new comment into the database.
	 *
	 * <p>
	 * This method receives a comment object via HTTP POST, validates the required fields, and attempts to save it 
	 * to the database. If all required fields are present and valid, the comment is saved. If any validation fails 
	 * or an error occurs during the saving process, an appropriate error message is returned.
	 * </p>
	 *
	 * @param request A {@link Comentario} object containing the details of the comment to be inserted. The fields of 
	 *                the comment must include:
	 *                - `titulo` (title)
	 *                - `texto` (text)
	 *                - `usuario` (user)
	 *                - `obra` (work)
	 *
	 * @return A {@link ResponseEntity} containing a JSON object with the status and message:
	 *         - If the comment is successfully inserted, the response will contain:
	 *           - `"status": "true"`, `"message": "Datos recibidos con éxito"`.
	 *         - If validation fails or an error occurs during saving, the response will contain:
	 *           - `"status": "false"` and an appropriate error message.
	 */
	@PostMapping("/postCommentInserted")
	public ResponseEntity<String> postCommentInserted(@RequestBody Comentario request) throws JsonProcessingException {
	    Map<String, String> response = new HashMap<>();
	    ResponseEntity<String> answer = ResponseEntity.ok("Entered to the back, but not completed");

	    // Validación de los campos del comentario
	    if (request.getTitulo() != null && !request.getTitulo().isEmpty() && 
	        request.getTexto() != null && !request.getTexto().isEmpty() &&  
	        request.getUsuario() != null && !request.getUsuario().isEmpty() && 
	        request.getObra() != null) {
	        	        	
	    	try {
	    		request.setFecha( LocalDateTime.now());
	    		request.setId(null);
	    		commentRepo.save(request); 
	    		response.put("status", "true");
	    		response.put("message", "Datos recibidos con éxito");
	    	} catch (Exception e) {
	    		response.put("status", "false");
		        response.put("message", "Error al enviar los datos a la base de datos");
	    	}
	    	
	       
	    } else {
	        
	        response.put("status", "false");
	        response.put("message", "Error al recibir los datos");
	    }
	    ObjectMapper om = new ObjectMapper();
	    String json= om.writeValueAsString(response);
	    return ResponseEntity.ok(json);
	}
	
	/**
	 * Retrieves the ID of a comment made by a user on a specific work (identified by ISBN).
	 *
	 * <p>
	 * This method handles HTTP GET requests and looks for a comment made by a specific user on a work identified 
	 * by its ISBN. If a comment is found, it returns the ID of the comment. If no comment is found or if an error 
	 * occurs during the process, an appropriate message is returned.
	 * </p>
	 *
	 * @param isbn The ISBN (unique identifier) of the work to look for comments on.
	 * @param usr The username (or user identifier) of the person who made the comment.
	 *
	 * @return A {@link ResponseEntity} containing a JSON object:
	 *         - If a comment is found, the JSON will include:
	 *           - `"status": "true"`, `"idComment": "<comment_id>"`.
	 *         - If no comment is found or if an error occurs, the JSON will include:
	 *           - `"status": "false"` and an appropriate error message.
	 */
	@GetMapping("/getIdComment")
	public ResponseEntity<String> getIdComment(@RequestParam Long isbn, @RequestParam String usr) throws JsonProcessingException{
		ResponseEntity<String> response = ResponseEntity.ok(null);
		ObjectMapper om = new ObjectMapper();
		Map<String, String> answer = new HashMap<>();
		
		if (isbn != null && usr != null) {
			try {
				Optional<Comentario> comment = commentRepo.findByObraAndUsuario(isbn, usr);
				if (!comment.isEmpty()) {
					answer.put("status", "true");
					answer.put("idComment", comment.get().getId());
				} else {
					answer.put("status", "false");
					answer.put("message", "There is not comment from that user in this writing");
					
				}
			} catch (Exception e) {
				answer.put("status", "false");
				answer.put("message", "There was an error getting the comment's id: "+e);
			}
		}
		String json = om.writeValueAsString(answer);
		return ResponseEntity.ok(json);
	}
	
	/**
	 * Retrieves the details of a comment by its ID.
	 *
	 * <p>
	 * This method handles HTTP GET requests and searches for a comment in the database using the provided comment ID.
	 * If the comment is found, it returns the comment's title, text, and rating. If no comment is found or an error occurs,
	 * an appropriate message is returned.
	 * </p>
	 *
	 * @param id The ID of the comment to be retrieved.
	 *
	 * @return A {@link ResponseEntity} containing a JSON object:
	 *         - If the comment is found, the JSON will include:
	 *           - `"status": "true"`, `"title": "<comment_title>"`, `"text": "<comment_text>"`, `"value": "<comment_rating>"`.
	 *         - If no comment is found, the JSON will include:
	 *           - `"status": "false"` and an appropriate error message.
	 *         - If there is an error in retrieving the comment, the JSON will include:
	 *           - `"status": "false"` and an error message describing the issue.
	 */
	@GetMapping("/getCommentEdit")
	public ResponseEntity<String> getCommentEdit(@RequestParam String id) throws JsonProcessingException{
		ObjectMapper om = new ObjectMapper();
		Map<String, String> answer = new HashMap<>();
		System.out.println("searching comment with id: "+id);
		if (id!=null) {
			try {
				Optional<Comentario> comment = commentRepo.findById(id);
				if (!comment.isEmpty()) {
					System.out.println("found the comment: "+comment.get().toString());
					answer.put("status", "true");
					answer.put("title", comment.get().getTitulo());
					answer.put("text", comment.get().getTexto());
					answer.put("value", String.valueOf(comment.get().getValoracion()));
				} else {
					answer.put("status", "false");
					answer.put("message", "Couldn't found a comment with the id: "+id);
				}
			} catch (Exception e) {
				answer.put("status", "false");
				answer.put("message", "There was an error getting the comment from the database");
			}
		} else {
			answer.put("status", "false");
			answer.put("message", "server not recieve id to search");
		}
		
		String json = om.writeValueAsString(answer);
		return ResponseEntity.ok(json);
	}
	
	/**
	 * Edits an existing comment in the database.
	 *
	 * <p>
	 * This method handles HTTP POST requests and updates an existing comment in the database. 
	 * It requires the full comment data to be provided, including the title, text, user, and associated work (obra).
	 * If the comment is found, it updates its details; if not, an error message is returned.
	 * </p>
	 *
	 * @param request The comment object containing the updated details to be saved.
	 * 
	 * @return A {@link ResponseEntity} containing a JSON object:
	 *         - If the comment is successfully updated:
	 *           - `"status": "true"`, `"message": "Datos recibidos con éxito"`.
	 *         - If no comment to edit is found in the database:
	 *           - `"status": "false"`, `"message": "There wasn't a comment to edit in the database"`.
	 *         - If there is an error during the update:
	 *           - `"status": "false"`, `"message": "Error al enviar los datos a la base de datos"`.
	 *         - If the request data is incomplete or invalid:
	 *           - `"status": "false"`, `"message": "Error al recibir los datos"`.
	 */
	@PostMapping("/postCommentEdited")
	public ResponseEntity<String> postCommentEdited(@RequestBody Comentario request) throws JsonProcessingException {
	    Map<String, String> response = new HashMap<>();
	    ResponseEntity<String> answer = ResponseEntity.ok("Entered to the back, but not completed");

	    // Validación de los campos del comentario
	    if (request.getTitulo() != null && !request.getTitulo().isEmpty() && 
	        request.getTexto() != null && !request.getTexto().isEmpty() &&  
	        request.getUsuario() != null && !request.getUsuario().isEmpty() && 
	        request.getObra() != null) {
	        
	    	try {
	    		
	    		Optional<Comentario> comment = commentRepo.findByObraAndUsuario(request.getObra(), request.getUsuario());
	    		if(!comment.isEmpty()) {
	    			request.setId(comment.get().getId());
	    			request.setFecha( LocalDateTime.now());
		    		request.setTipo(comment.get().getTipo());
		    		commentRepo.save(request); 
		    		response.put("status", "true");
		    		response.put("message", "Datos recibidos con éxito");
	    		} else {
	    			response.put("status", "false");
	    			response.put("message", "There wasn't a comment to edit in database");
	    		}
	    		
	    		
	    	} catch (Exception e) {
	    		response.put("status", "false");
		        response.put("message", "Error al enviar los datos a la base de datos");
	    	}
	    	
	       
	    } else {
     
	        response.put("status", "false");
	        response.put("message", "Error al recibir los datos");
	    }
	    ObjectMapper om = new ObjectMapper();
	    String json= om.writeValueAsString(response);
	    return ResponseEntity.ok(json);
	}
	
	/**
	 * Checks if an email is already registered in the system.
	 *
	 * <p>
	 * This method handles HTTP GET requests to check if the provided email is already registered in the system.
	 * It validates the format of the email, then searches the database for any existing entries with that email.
	 * If the email exists, the response indicates that the email is already registered. Otherwise, the email is available for registration.
	 * </p>
	 *
	 * @param email The email address to be checked.
	 * 
	 * @return A {@link ResponseEntity} containing a JSON object with the status and message:
	 *         - If the email is valid and already registered:
	 *           - `"status": "true"`, `"checkEmail": "false"`, `"resp": "This email is already registered, try to log-in"`.
	 *         - If the email is valid and not registered:
	 *           - `"status": "true"`, `"checkEmail": "true"`.
	 *         - If the email format is invalid (does not contain "@" or does not end with ".com" or ".es"):
	 *           - `"status": "true"`, `"checkEmail": "false"`.
	 *         - If the email is null:
	 *           - `"status": "false"`, `"message": "The email received is null"`.
	 *         - If there is an error while searching for emails in the database:
	 *           - `"status": "false"`, `"message": "Error searching emails in database"`.
	 */
	@GetMapping("/getEmails")
	public ResponseEntity<String> getEmails(@RequestParam String email) throws JsonProcessingException{
		Map<String, String> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    
	    if (email != null) {
	    	
	    	if(email.contains("@") && email.matches(".*\\.(com|es)$")) {
	    		try {
	    			List<Usuario> emails = userRepo.findAllEmailsOnly();
	    			if (!emails.isEmpty()) {
	    				/*Check emails and stops if finds any match*/
	    				boolean isPresent = emails.stream().anyMatch(user -> user.getEmail().equals(email));
	    				if(isPresent==true) {
	    					rs.put("status", "true");
	    					rs.put("checkEmail", "false");
	    					rs.put("resp", "This email is already registered, try to log-in");
	    				} else {
	    					rs.put("status", "true");
	    					rs.put("checkEmail", "true");
	    				}
	    			}
		    	} catch (Exception e) {
			    	rs.put("status", "false");
			    	rs.put("message", "Error searching emails in database");
		    	}
	    	} else {
	    		rs.put("status","true");
	    		rs.put("checkEmail", "false");
	    	}
	 
	    } else {
	    	rs.put("status", "false");
	    	rs.put("message", "The email received is null: "+email);
	    }
	    
	    String json= om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	/**
	 * Checks if an email is already registered, excluding the current email.
	 *
	 * <p>
	 * This method handles HTTP GET requests to check if the provided email is already registered in the system,
	 * while excluding the user's current email (to allow for email changes). It validates the format of the email,
	 * searches the database for any existing entries with that email, and compares it to the current email.
	 * If the email exists, the response indicates that the email is already registered. Otherwise, the email is available for registration.
	 * </p>
	 *
	 * @param email The email address to be checked.
	 * @param currentEmail The current email of the user, which will be excluded from the check.
	 * 
	 * @return A {@link ResponseEntity} containing a JSON object with the status and message:
	 *         - If the email is valid and already registered (excluding the current email):
	 *           - `"status": "true"`, `"checkEmail": "false"`, `"resp": "This email is already registered, try to log-in"`.
	 *         - If the email is valid and not registered (excluding the current email):
	 *           - `"status": "true"`, `"checkEmail": "true"`.
	 *         - If the email format is invalid (does not contain "@" or does not end with ".com" or ".es"):
	 *           - `"status": "true"`, `"checkEmail": "false"`.
	 *         - If the email is null:
	 *           - `"status": "false"`, `"message": "The email received is null"`.
	 *         - If there is an error while searching for emails in the database:
	 *           - `"status": "false"`, `"message": "Error searching emails in database"`.
	 */
	@GetMapping("/getEmailsEdit")
	public ResponseEntity<String> getEmailsEdit(@RequestParam String email, @RequestParam String currentEmail) throws JsonProcessingException{
		Map<String, String> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    
	    if (email != null) {
	    	
	    	if(email.contains("@") && email.matches(".*\\.(com|es)$")) {
	    		try {
	    			List<Usuario> emails = userRepo.findAllEmailsOnly();
	    			
	    			if (!emails.isEmpty()) {
	    				emails.removeIf(user -> user.getEmail().equals(currentEmail));
	    				/*Check emails and stops if finds any match*/
	    				boolean isPresent = emails.stream().anyMatch(user -> user.getEmail().equals(email));
	    				if(isPresent==true) {
	    					rs.put("status", "true");
	    					rs.put("checkEmail", "false");
	    					rs.put("resp", "This email is already registered, try to log-in");
	    				} else {
	    					rs.put("status", "true");
	    					rs.put("checkEmail", "true");
	    				}
	    			}
		    	} catch (Exception e) {
			    	rs.put("status", "false");
			    	rs.put("message", "Error searching emails in database");
		    	}
	    	} else {
	    		rs.put("status","true");
	    		rs.put("checkEmail", "false");
	    	}
	 
	    } else {
	    	rs.put("status", "false");
	    	rs.put("message", "The email received is null: "+email);
	    }
	    
	    String json= om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	/**
	 * Retrieves information about a user based on their ID, returning data specific to their role.
	 *
	 * <p>
	 * This method handles HTTP GET requests to fetch details of a user, including name, email, birthdate,
	 * and role-specific information. The user's role determines what additional data is included:
	 * - For "STUDENT" users: studies and school information.
	 * - For "TITLED" users: details about their studies and title.
	 * - For "ADMIN" users: phone number.
	 * If the user cannot be found or if an error occurs during the process, an appropriate error message is returned.
	 * </p>
	 *
	 * @param id The unique identifier of the user whose information is to be retrieved.
	 * 
	 * @return A {@link ResponseEntity} containing a JSON object with the status and user details:
	 *         - If the user is found:
	 *           - "status": "true", with corresponding user details such as:
	 *             - "name": User's name.
	 *             - "email": User's email address.
	 *             - "fechaNac": User's birthdate.
	 *             - "role": User's role.
	 *             - Role-specific data (e.g., "studies", "school", "studiesTitle", etc.).
	 *         - If the user cannot be found:
	 *           - "status": "false", "message": "Couldn´t find the user with id: {id}".
	 *         - If the user has no role:
	 *           - "status": "false", "message": "The user collected has no role".
	 *         - If there is an exception while retrieving the user:
	 *           - "status": "false", "message": "Exception in server getting the user to edit: {exception}".
	 */
	@GetMapping("/getUserToEdit")
	public ResponseEntity<String> getUserToEdit(@RequestParam String id) throws JsonProcessingException{
		Map<String, String> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    
	    try {
	    	//Find the user by its id
	    	Optional<Usuario> user = userRepo.findById(id);
	    	if(!user.isEmpty()) {
	    		// Verifying user role
	    		if(user.get().getRole().equals("STUDENT")) {
		    		rs.put("status", "true");
		    		rs.put("name", user.get().getName());
		    		rs.put("email", user.get().getEmail());
		    		rs.put("fechaNac", user.get().getFechaNac().toString());
		    		rs.put("role", user.get().getRole());
		    		
		    		rs.put("studies", (user != null && user.get().getStudies() != null) ? user.get().getStudies() : "");
		    		rs.put("school", (user != null && user.get().getSchool() != null) ? user.get().getSchool() : "");

	    		} else if (user.get().getRole().equals("TITLED")) {
		    		rs.put("status", "true");
		    		rs.put("name", user.get().getName());
		    		rs.put("email", user.get().getEmail());
		    		rs.put("fechaNac", user.get().getFechaNac().toString());
		    		rs.put("role", user.get().getRole());
		    		
		    		rs.put("studiesTitle", (user != null && user.get().getStudies_title() != null) ? user.get().getStudies_title() : "");
		    		rs.put("studyPlace", (user != null && user.get().getStudy_place() != null) ? user.get().getStudy_place() : "");
		    		rs.put("titleDate", (user != null && user.get().getTitle_date() != null) ? user.get().getTitle_date().toString() : "");
	    		} else if (user.get().getRole().equals("ADMIN")) {
		    		rs.put("status", "true");
		    		rs.put("name", user.get().getName());
		    		rs.put("email", user.get().getEmail());
		    		rs.put("fechaNac", user.get().getFechaNac().toString());
		    		rs.put("role", user.get().getRole());
		    		
		    		rs.put("phone", (user != null && user.get().getPhone() != null) ? user.get().getPhone().toString() : "");
	    		} else {
	    			rs.put("status", "false");
	    			rs.put("message", "The user collected has not role");
	    		}
	    		
	    	} else {
	    		rs.put("status", "false");
	    		rs.put("message", "Couldn´t find the user with id: "+id);
	    	}
	    } catch (Exception e) {
	    	rs.put("status", "false");
	    	rs.put("message", "Exception in server getting the user to edit: "+e);
	    }
	    
	    String json= om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	/**
	 * Edits an existing user by updating their information with the provided details.
	 *
	 * <p>
	 * This method handles HTTP POST requests to edit an existing user in the system. The user's details are 
	 * updated based on the data provided in the request body. Fields such as name, password, role, birthdate, 
	 * email, studies, school, title information, and phone number are updated if they contain valid values.
	 * The password, if provided, is encoded before saving. After successfully saving the updated user information, 
	 * a status message is returned indicating whether the operation was successful or if there was an error.
	 * </p>
	 *
	 * @param user A {@link Usuario} object containing the new data to update the user.
	 *             The user object should include the user ID and any fields that need to be updated.
	 * 
	 * @return A {@link ResponseEntity} containing a JSON object with the status of the operation:
	 *         - If the user is successfully updated:
	 *           - "status": "true".
	 *         - If there is an error during the process:
	 *           - "status": "false", "message": "There was an error editing the user in server: {error message}".
	 */
	@PostMapping("/postUserEdited")
	public ResponseEntity<String> postUserEdited(@RequestBody Usuario user) throws JsonProcessingException{
		Map<String, String> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    
	    Usuario editedUser = new Usuario();
	    System.out.println("Editando: "+user.toString());
	    editedUser.setId(user.getId());
	    if (StringUtils.hasText(user.getPassword())) {
	    	editedUser.setPassword(encoder.encode(user.getPassword()));
	    }
	    if(StringUtils.hasText(user.getName())) {
	    	editedUser.setName(user.getName());
	    }
	    if(StringUtils.hasText(user.getRole())) {
	    	editedUser.setRole(user.getRole());
	    }
	    if(StringUtils.hasText(user.getFechaNac().toString())) {
	    	editedUser.setFechaNac(user.getFechaNac());
	    }
	    if(StringUtils.hasText(user.getEmail())) {
	    	editedUser.setEmail(user.getEmail());
	    }
	    if(StringUtils.hasText(user.getStudies())) {
	    	editedUser.setStudies(user.getStudies());
	    }
	    if(StringUtils.hasText(user.getSchool())) {
	    	editedUser.setSchool(user.getSchool());
	    }
	    if(StringUtils.hasText(user.getStudies_title())) {
	    	editedUser.setStudies_title(user.getStudies_title());
	    }
	    if(StringUtils.hasText(user.getStudy_place())) {
	    	editedUser.setStudy_place(user.getStudy_place());
	    }
	    if(user.getTitle_date()!=null) {
	    	if(StringUtils.hasText(user.getTitle_date().toString())) {
	    		editedUser.setTitle_date(user.getTitle_date());
	    	}
	    }
	    
	    if(user.getPhone()!=null) {
		    if(StringUtils.hasText(user.getPhone().toString())) {
		    	editedUser.setPhone(user.getPhone());
		    }
	    }
	    
	    try {
	    	userRepo.save(editedUser);
	    	rs.put("status", "true");
	    } catch(Exception e) {
	    	rs.put("status", "false");
	    	rs.put("message", "There was an error editing the user in server: "+e);
	    }
	     
	    String json= om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	/**
	 * Registers a new user in the system by saving their details to the database.
	 *
	 * <p>
	 * This method handles HTTP POST requests to create a new user. It ensures that the user is not already logged 
	 * in before allowing the registration process. If the user is authenticated and already logged in, 
	 * a message indicating that the user is already logged is returned.
	 * The provided user details (name, email, password, birthdate, school, and studies) are validated, 
	 * and the user is assigned a default role of "STUDENT". The password is encoded before being stored.
	 * If the user details are valid, the user is saved to the database; otherwise, an error message is returned.
	 * </p>
	 *
	 * @param user A {@link Usuario} object containing the user details to be registered.
	 *             The object must include the user's name, email, password, birthdate, school, and studies.
	 * 
	 * @return A {@link ResponseEntity} containing a JSON object with the status of the operation:
	 *         - If the user is already logged in:
	 *           - "status": "false", "message": "You are already logged!".
	 *         - If the user details are incomplete or invalid:
	 *           - "status": "false", "message": "Server didn't receive a user to log".
	 *         - If the user is successfully registered:
	 *           - "status": "true".
	 *         - If there is an error while saving the user:
	 *           - "status": "false", "message": "Couldn't insert the new user in database".
	 */
	@PostMapping("/postRegistryUser")
	public ResponseEntity<String> postRegistryUser(@RequestBody Usuario user) throws JsonProcessingException{
		Map<String, String> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            rs.put("status", "false");
            rs.put("message", "You are already logged!");
        } else {
        	if(user!=null && user.getName()!=null && user.getEmail()!=null && user.getPassword()!=null && user.getFechaNac()!=null && user.getSchool()!=null && user.getStudies()!=null) {
        		 // Preparing the new user with the provided details
        		user.setId(null);
        		user.setPassword(encoder.encode(user.getPassword())); // Encode the password
        		user.setRole("STUDENT");
        		
        		try {
        			userRepo.save(user);
        			rs.put("status", "true");
        		} catch (Exception e) {
                    rs.put("status", "false");
                    rs.put("message", "Couldn't insert the new user in database");
        		}
        	} else {
                rs.put("status", "false");
                rs.put("message", "Server didn't recieve an user to log");
        	}
        }
	    
	    String json= om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	/**
	 * Retrieves all users' details from the database.
	 *
	 * <p>
	 * This method handles HTTP GET requests to fetch a list of all users from the database. 
	 * The users are returned in an array format with all available user details. If no users are found, 
	 * an error message is returned indicating that there are no users in the database. 
	 * If an error occurs while fetching the users, an error message is returned.
	 * </p>
	 *
	 * @return A {@link ResponseEntity} containing a JSON object with the status and user details:
	 *         - If the users are successfully retrieved:
	 *           - "status": "true", "array": An array containing all the user details.
	 *         - If no users are found:
	 *           - "status": "false", "message": "There are no users in database, this might be an error".
	 *         - If there is an error fetching the users:
	 *           - "status": "false", "message": "There was an error in server trying to fetch the users".
	 */
	@GetMapping("/getAllIdUsers")
	public ResponseEntity<String> getAllIdUsers() throws JsonProcessingException{
		Map<String, Object> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    // those are modifications for ObjectMapper to allow Jackson library to read DataTime objects, first allow to read and the second formats it to have - between each value (Year-Month-Day)
	    om.registerModule(new JavaTimeModule());
	    om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	    try {
	    	List<Usuario> users = userRepo.findAll();
	    	if (!users.isEmpty()) {
	    		rs.put("status", "true");
	    		rs.put("array", users);
	    	} else {
	    		rs.put("status", "false");
		    	rs.put("message", "There are no users in database, this might be an error");
	    	}
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	rs.put("status", "false");
	    	rs.put("message", "There was an error in server trying to fetch the users");
	    }
	    String json= om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	/**
	 * Retrieves all works (or writings) from the database.
	 *
	 * <p>
	 * This method handles HTTP GET requests to fetch a list of all works (or writings) stored in the database.
	 * The works are returned in an array format. If no works are found, an error message is returned indicating 
	 * that there are no writings in the database. If an error occurs while fetching the works, an error message is returned.
	 * </p>
	 *
	 * @return A {@link ResponseEntity} containing a JSON object with the status and works details:
	 *         - If the works are successfully retrieved:
	 *           - "status": "true", "array": An array containing all the works details.
	 *         - If no works are found:
	 *           - "status": "false", "message": "There are no Writings in database, this might be an error".
	 *         - If there is an error fetching the works:
	 *           - "status": "false", "message": "There was an error in server trying to fetch the writings".
	 */
	@GetMapping("/getAllIdWorks")
	public ResponseEntity<String> getAllIdWorks() throws JsonProcessingException{
		Map<String, Object> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    // Configure ObjectMapper to handle DateTime objects correctly
	    om.registerModule(new JavaTimeModule());
	    om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	    try {
	    	List<Obra> works = obraRepo.findAll();
	    	if (!works.isEmpty()) {
	    		
	    		System.out.println("writings to show: "+works.toString());
	    		
	    		System.out.println(works.toString());
	    		rs.put("status", "true");
	    		rs.put("array", works);
	    		
	    	} else {
	    		rs.put("status", "false");
		    	rs.put("message", "There are no Writings in database, this might be an error");
	    	}
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	rs.put("status", "false");
	    	rs.put("message", "There was an error in server trying to fetch the writings");
	    }
	    String json= om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	/**
	 * Deletes a user and their associated comments from the system.
	 *
	 * <p>
	 * This method handles HTTP GET requests to delete a user based on their user ID. 
	 * It first verifies that the provided ID is not null. If the ID is valid, it fetches and deletes all comments 
	 * associated with the user and then deletes the user itself from the database. 
	 * If the ID is null or an error occurs during the process, an appropriate error message is returned.
	 * </p>
	 *
	 * @param id The ID of the user to be deleted.
	 * 
	 * @return A {@link ResponseEntity} containing a JSON object with the status of the operation:
	 *         - If the user and their comments are successfully deleted:
	 *           - "status": "true".
	 *         - If there is an error during the deletion process:
	 *           - "status": "false", "message": "Error connecting with server, deleting the user aborted".
	 *         - If the provided ID is null:
	 *           - "status": "false", "message": "The id received to delete is empty".
	 */
	@GetMapping("/getUserDeleted")
	public ResponseEntity<String> getDeletedUser(@RequestParam String id) throws JsonProcessingException{
		Map<String, String> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    if (id!=null) {
	    	try {
	    		
	    		List<Comentario> comments = commentRepo.findAllByUsuario(id);
	    		for (Comentario comment : comments) {
	    			commentRepo.deleteById(comment.getId());
	    		}
	    		userRepo.deleteById(id);
	    		rs.put("status", "true");
		    } catch (Exception e) {
		    	rs.put("status", "false");
		    	rs.put("message", "Error connecting with server, deleting the user aborted");
		    }
	    } else {
	    	rs.put("status", "false");
	    	rs.put("message", "The id recieved to delete is empty");
	    }
	    
	    String json= om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	/**
	 * Deletes a work (or writing) and its associated comments from the system.
	 *
	 * <p>
	 * This method handles HTTP GET requests to delete a work (or writing) based on its ID. 
	 * It first verifies that the provided ID is not null. If the ID is valid, it fetches and deletes all comments 
	 * associated with the work and then deletes the work itself from the database. 
	 * If the ID is null or an error occurs during the process, an appropriate error message is returned.
	 * </p>
	 *
	 * @param id The ID of the work (or writing) to be deleted.
	 * 
	 * @return A {@link ResponseEntity} containing a JSON object with the status of the operation:
	 *         - If the work and its comments are successfully deleted:
	 *           - "status": "true".
	 *         - If there is an error during the deletion process:
	 *           - "status": "false", "message": "Error connecting with server, deleting the writing aborted".
	 *         - If the provided ID is null:
	 *           - "status": "false", "message": "The id received to delete is empty".
	 */
	@GetMapping("/getWorkDeleted")
	public ResponseEntity<String> getWorkDeleted(@RequestParam Long id) throws JsonProcessingException{
		Map<String, String> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    if (id!=null) {
	    	try {
	    		
	    		List<Comentario> comments = commentRepo.findAllByObra(id);
	    		for (Comentario comment : comments) {
	    			commentRepo.deleteById(comment.getId());
	    		}
	    		obraRepo.deleteById(id);
	    		rs.put("status", "true");
		    } catch (Exception e) {
		    	rs.put("status", "false");
		    	rs.put("message", "Error connecting with server, deleting the writing aborted");
		    }
	    } else {
	    	rs.put("status", "false");
	    	rs.put("message", "The id recieved to delete is empty");
	    }
	    
	    String json= om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	/**
	 * Retrieves the details of a work (writing) to edit based on its ID.
	 *
	 * <p>
	 * This method handles HTTP GET requests to fetch the details of a work (writing) from the database 
	 * based on the provided ID. It returns specific information depending on the type of work (e.g., book or article). 
	 * If the work is found, the details such as title, author, publication date, type, abstract, and other relevant information 
	 * are returned. If the work is not found or there is an issue during the process, an appropriate error message is returned.
	 * </p>
	 *
	 * @param id The ID (ISBN) of the work (writing) to be retrieved.
	 * 
	 * @return A {@link ResponseEntity} containing a JSON object with the status and work details:
	 *         - If the work is successfully retrieved:
	 *           - "status": "true", with fields for title, author, publication date, type, abstract, and other details.
	 *         - If the work is not found:
	 *           - "status": "false", "message": "Couldn't find the Writing with isbn: [id]".
	 *         - If the work does not have a valid type:
	 *           - "status": "false", "message": "The Writing collected has no type".
	 *         - If there is an exception while retrieving the work:
	 *           - "status": "false", "message": "Exception in server getting the writing to edit: [exception details]".
	 */
	@GetMapping("/getWorkToEdit")
	public ResponseEntity<String> getWorkToEdit(@RequestParam Long id) throws JsonProcessingException{
		Map<String, String> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    
	    try {
	    	Optional<Obra> obra = obraRepo.findById(id);
	    	if(!obra.isEmpty()) {
	    		if(obra.get().getTipo().equals("BOOK")) {
		    		rs.put("status", "true");
		    		rs.put("title", obra.get().getTitulo());
		    		rs.put("autor", obra.get().getAutor());
		    		rs.put("publicationDate", obra.get().getFechaPublicacion().toString());
		    		rs.put("type", obra.get().getTipo());
		    		rs.put("abstract", obra.get().getAbstracto());
		    		rs.put("editorial", obra.get().getEditorial());
		    		rs.put("publicationPlace", obra.get().getLugar_publicacion());
		    		rs.put("temas", om.writeValueAsString(obra.get().getTemas()));

	    		} else if (obra.get().getTipo().equals("ARTICLE")) {
		    		rs.put("status", "true");
		    		rs.put("title", obra.get().getTitulo());
		    		rs.put("autor", obra.get().getAutor());
		    		rs.put("publicationDate", obra.get().getFechaPublicacion().toString());
		    		rs.put("type", obra.get().getTipo());
		    		rs.put("abstract", obra.get().getAbstracto());
		    		rs.put("editorial", obra.get().getEditorial());
		    		rs.put("publicationPlace", obra.get().getLugar_publicacion());
		    		rs.put("temas", om.writeValueAsString(obra.get().getTemas()));
		    		rs.put("PageIni", String.valueOf(obra.get().getPaginaini()));
		    		rs.put("PageFin", String.valueOf(obra.get().getPaginafin()));
	    		} else {
	    			rs.put("status", "false");
	    			rs.put("message", "The Writing collected has not type");
	    		}
	    		
	    	} else {
	    		rs.put("status", "false");
	    		rs.put("message", "Couldn´t find the Writing with isbn: "+id);
	    	}
	    } catch (Exception e) {
	    	rs.put("status", "false");
	    	rs.put("message", "Exception in server getting the writing to edit: "+e);
	    }
	    
	    String json= om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	/**
	 * Edits and updates the details of a work (writing) in the database.
	 *
	 * <p>
	 * This method handles HTTP POST requests to edit an existing work (writing) based on the provided data. 
	 * It performs validation on the required fields of the work (ISBN, title, author, editorial, publication date, type, abstract, and themes). 
	 * If any required field is empty or invalid, the method returns an error message with the missing or incorrect fields. 
	 * If all fields are valid, the method updates the work in the database. In case of any errors during the saving process, an error message is returned.
	 * </p>
	 *
	 * @param work The work (writing) object containing the updated details to be saved.
	 * 
	 * @return A {@link ResponseEntity} containing a JSON object with the status and message:
	 *         - If the work is successfully edited:
	 *           - "status": "true".
	 *         - If any required field is empty or invalid:
	 *           - "status": "false", "message": "Any writing's field is empty: [list of missing fields]".
	 *         - If there is an error during the save process:
	 *           - "status": "false", "message": "There was an error editing the work in server: [error details]".
	 */
	@PostMapping("/postWorkEdited")
	public ResponseEntity<String> postWorkEdited(@RequestBody Obra work) throws JsonProcessingException{
		Map<String, String> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    System.out.println("Editing Work...: "+work.toString());
	    boolean cell = true;
	    String fields = "";
	    if (work.getIsbn()==null) {
	    	cell=false;
	    	fields="ISBN is empty\n";
	    }
	    if(work.getTitulo()==null || work.getTitulo()=="") {
	    	cell=false;
	    	fields+="Title is empty\n";
	    }
	    if (work.getAutor()==null || work.getAutor()=="") {
	    	cell=false;
	    	fields+="Autor is empty\n";

	    }
	    if (work.getEditorial()==null || work.getEditorial()=="") {
	    	cell=false;
	    	fields+="Editorial is empty\n";
	    }
	    if (work.getFechaPublicacion()==null) {
	    	cell=false;
	    	fields+="FechaPublicacion is empty\n";	    
	    }
	    if (work.getLugar_publicacion()==null||work.getLugar_publicacion()=="") {
	    	cell=false;
	    	fields+="Lugar_publicacion is empty\n";
	    }
	    if (work.getTipo()==null||work.getTipo()=="") {
	    	cell=false;
	    	fields+="Tipo is empty\n";
	    }
	    if (!work.getTipo().equals("BOOK") && !work.getTipo().equals("ARTICLE")) {
	    	cell=false;
	    	fields+="Tipo not equals BOOK nor ARTICLE\n";
	    }
	    if (work.getAbstracto()==null || work.getAbstracto()=="") {
	    	cell=false;
	    	fields+="Astracto is empty\n";
	    }
	    if (work.getTemas()==null || work.getTemas().isEmpty()) {
	    	cell=false;
	    	fields+="Temas is empty\n";
	    }
	    if (cell==true) {
	    	try {
		    	obraRepo.save(work);
		    	rs.put("status", "true");
		    } catch(Exception e) {
		    	rs.put("status", "false");
		    	rs.put("message", "There was an error editing the user in server: "+e);
		    }
	    } else {
	    	rs.put("status", "false");
	    	rs.put("message", "Any writing's field is empty: "+fields);
	    }
	    
	     
	    String json= om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	/**
	 * Searches for works (writings) in the database based on the provided search parameter.
	 *
	 * <p>
	 * This method handles HTTP GET requests to search for works (writings) that match the search parameter.
	 * The search term is passed as a query parameter. It performs a search on the works based on the provided search 
	 * parameter using the repository method `findAllParams`. The method returns a list of works that match the search term.
	 * If any error occurs during the search process, the method returns an error message.
	 * </p>
	 *
	 * @param search The search term used to filter works in the database.
	 * 
	 * @return A {@link ResponseEntity} containing a JSON object with the status and the list of works:
	 *         - If works are successfully fetched:
	 *           - "status": "true", "array": [list of works].
	 *         - If there is an error during the fetch process:
	 *           - "status": "false", "message": "There was an error in server trying to fetch the writings".
	 */
	@GetMapping("getSearchWorkList")
	public ResponseEntity<String> getSearchWorkList(@RequestParam String search) throws JsonProcessingException{
		Map<String, Object> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    // those are modifications for ObjectMapper to allow Jackson library to read DataTime objects, first allow to read and the second formats it to have - between each value (Year-Month-Day)
	    om.registerModule(new JavaTimeModule());
	    om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	    try {
	    	List<Obra> works = obraRepo.findAllParams(search);
	    
	    	System.out.println("writings to show: "+works.toString());
	    		
	    	System.out.println(works.toString());
	    	rs.put("status", "true");
	    	rs.put("array", works);
	    		
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	rs.put("status", "false");
	    	rs.put("message", "There was an error in server trying to fetch the writings");
	    }
	    String json= om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	/**
	 * Searches for users in the database based on the provided search parameter.
	 *
	 * <p>
	 * This method handles HTTP GET requests to search for users that match the search parameter.
	 * The search term is passed as a query parameter. It performs a search on the users using the repository method 
	 * `findAllParams`. The method returns a list of users that match the search term.
	 * If any error occurs during the search process, the method returns an error message.
	 * </p>
	 *
	 * @param search The search term used to filter users in the database.
	 * 
	 * @return A {@link ResponseEntity} containing a JSON object with the status and the list of users:
	 *         - If users are successfully fetched:
	 *           - "status": "true", "array": [list of users].
	 *         - If there is an error during the fetch process:
	 *           - "status": "false", "message": "There was an error in server trying to fetch the users".
	 */
	@GetMapping("/getSearchUsersList")
	public ResponseEntity<String> getSearchUsersList(@RequestParam String search) throws JsonProcessingException {
	    Map<String, Object> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    om.registerModule(new JavaTimeModule());
	    om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	    
	    try {
	        List<Usuario> users = userRepo.findAllParams(search); 

	        rs.put("status", "true");
	        rs.put("array", users);
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        rs.put("status", "false");
	        rs.put("message", "There was an error in server trying to fetch the users");
	    }
	    
	    String json = om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	/**
	 * Searches for comments in the database based on the provided search parameter.
	 *
	 * <p>
	 * This method handles HTTP GET requests to search for comments that match the search parameter.
	 * The search term is passed as a query parameter. The method sanitizes the search term by removing 
	 * all non-alphanumeric characters before performing the search. It uses the repository method `findAllParams`
	 * to search for comments in the database. If any comment is flagged as a banned comment or banned answer, 
	 * the method retrieves the corresponding user details and constructs a list of `ComentarioPintado` objects.
	 * </p>
	 *
	 * @param search The search term used to filter comments in the database. The search term is sanitized 
	 *               to remove any non-alphanumeric characters before performing the search.
	 * 
	 * @return A {@link ResponseEntity} containing a JSON object with the status and the list of comments:
	 *         - If comments are found and successfully fetched:
	 *           - "status": "true", "array": [list of comments].
	 *         - If no comments are found, an empty array is returned:
	 *           - "status": "true", "array": [].
	 *         - If there is an error during the fetch process:
	 *           - "status": "false", "message": "There was an error in server trying to fetch the comments".
	 */
	@GetMapping("/getSearchCommentList")
	public ResponseEntity<String> getSearchCommentList(@RequestParam String search) throws JsonProcessingException {
	    Map<String, Object> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    om.registerModule(new JavaTimeModule());
	    om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	    
	    search = search.replaceAll("[^a-zA-Z0-9]", "");
	    try {
	        List<Comentario> commentsData = commentRepo.findAllParams(search); 
	        List<ComentarioPintado> comments = new ArrayList<ComentarioPintado>();
	        if (!commentsData.isEmpty()) {
	        	for(Comentario comment : commentsData) {
	        		if (comment.getTipo().equals("BANNED_COMMENT") || comment.getTipo().equals("BANNED_ANSWER")) {

		        		Optional<Usuario> user = userRepo.findById(comment.getUsuario());
		        		if (user.isPresent()) {

		        			comments.add(new ComentarioPintado(comment, user.get().getId(), user.get().getUsername(), user.get().getRole()));
		        		}
		        		
	        		}
	        	}
	        }
	        if (!comments.isEmpty()) {
	        	System.out.println("Found Comments with that search");
	        	rs.put("status", "true");
	        	rs.put("array", comments);
	        } else {
	        	System.out.println("Not found comments with that search");
	        	rs.put("status", "true");
	        	rs.put("array", comments);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        rs.put("status", "false");
	        rs.put("message", "There was an error in server trying to fetch the users");
	    }
	    
	    String json = om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	/**
	 * Checks if an ISBN is present in the database.
	 *
	 * <p>
	 * This method handles HTTP GET requests to verify whether a given ISBN (identified by the `id` parameter)
	 * exists in the database. It retrieves all ISBNs from the database and checks if the provided `id` matches
	 * any of the ISBNs. The response includes the status of the check, indicating whether the ISBN is present.
	 * </p>
	 *
	 * @param id The ISBN to check for existence in the database.
	 * 
	 * @return A {@link ResponseEntity} containing a JSON object with the result of the ISBN check:
	 *         - "status": "true", if the check was successful.
	 *         - "present": "true", if the ISBN exists in the database, or "false" if it doesn't.
	 *         - "status": "false", and an error message if there was an issue during the operation.
	 */
	@GetMapping("/geIsbnChecked")
	public ResponseEntity<String> getIsbnChecked(@RequestParam Long id) throws JsonProcessingException {
		Map<String, String> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    
	    try {
	    	List<Obra> obras = obraRepo.findAllOnlyIsbn();
	    	if (obras.isEmpty()) {
	    		rs.put("status", "false");
	    		rs.put("message", "Error, no any ISBN received from database");
	    	} else {

	    		boolean isPresent = obras.stream().anyMatch(obra -> obra.getIsbn().equals(id));

	    		rs.put("status", "true");
	    		if (isPresent) {
	    			rs.put("present", "true");
	    		} else {
	    			rs.put("present", "false");
	    		}
	    		
	    	}

	    } catch (Exception e) {
	    	rs.put("status", "false");
	    	rs.put("message", "there was a problem in server: "+e);
	    }
		
		String json = om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	/**
	 * Inserts a new work into the database.
	 *
	 * <p>
	 * This method handles HTTP POST requests to insert a new work (book or article) into the database. The work
	 * is provided in the request body. It checks whether all required fields of the work (ISBN, title, author, 
	 * abstract, editorial, publication place, type, and publication date) are provided. If any field is missing,
	 * it returns an error message. If all fields are valid, the work is saved into the database.
	 * </p>
	 *
	 * @param work The {@link Obra} object containing the work details to be inserted into the database.
	 * 
	 * @return A {@link ResponseEntity} containing a JSON object:
	 *         - "status": "true", if the work was successfully inserted.
	 *         - "status": "false", and an error message if any required field was missing or there was a problem with the database.
	 */
	@PostMapping("/postWorkInsert")
	public ResponseEntity<String> postWorkInsert(@RequestBody Obra work) throws JsonProcessingException {
		Map<String, String> rs = new HashMap<>();
	    ObjectMapper om = new ObjectMapper();
	    
	    System.out.println("obra a insertar: "+work);
	    
	    if (work.getIsbn()!=null && 
	    	work.getTitulo()!=null && work.getTitulo()!="" &&
	    	work.getAutor()!=null && work.getAutor()!="" &&
	    	work.getAbstracto()!=null && work.getAbstracto()!="" &&
	    	work.getEditorial()!=null && work.getEditorial()!="" &&
	    	work.getLugar_publicacion()!=null && work.getLugar_publicacion()!="" &&
	    	work.getTipo()!=null && work.getTipo()!="" &&
	    	work.getFechaPublicacion()!=null) {
	    	
	    	try {
	    		obraRepo.save(work);
	    		rs.put("status", "true");
	    	} catch (Exception e) {
	    		rs.put("status", "false");
		    	rs.put("message", "There was a problem connecting with Database: "+e);
	    	}
	    	
	    } else {
	    	rs.put("status", "false");
	    	rs.put("message", "Any Writing field was empty, couldn't insert the writing in Database");
	    }
		
		String json = om.writeValueAsString(rs);
	    return ResponseEntity.ok(json);
	}
	
	/**
	 * Calculates the average rating of a work based on its comments.
	 *
	 * <p>
	 * This method retrieves all comments associated with a work (identified by its ID) and calculates the average
	 * rating (valoración) from those comments. If there are no comments for the work, the method returns a rating of 0.
	 * The average rating is calculated by summing the ratings of all comments and dividing it by the total number of comments.
	 * </p>
	 *
	 * @param id The ID of the work for which the average rating is to be calculated.
	 * 
	 * @return The average rating (valoración) of the work, or 0 if there are no comments.
	 */
	private int getAVG(Long id) {
		int valor=0;
		
		List<Comentario> valores = commentRepo.findAllByObraOnlyComment(id);
		if(!valores.isEmpty()) {
			int suma=0;
			int contador=0;
			for (Comentario comment : valores) {
				suma = suma + comment.getValoracion();
				contador++;
			}
			valor=suma/contador;
		}
		
		return valor;
	}
	
}
