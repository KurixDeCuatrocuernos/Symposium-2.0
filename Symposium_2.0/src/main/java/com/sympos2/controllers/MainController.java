package com.sympos2.controllers;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sympos2.dto.ComentarioPintado;
import com.sympos2.dto.ObraIsbnTituloProjection;
import com.sympos2.dto.UsuarioComentarioPintado;
import com.sympos2.models.Comentario;
import com.sympos2.models.Obra;
import com.sympos2.models.Usuario;
import com.sympos2.repositories.ComentarioRepository;
import com.sympos2.repositories.ObraRepository;
import com.sympos2.repositories.UserRepository;
import com.sympos2.services.ComentarioService;
import com.sympos2.services.ObraService;
import com.sympos2.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

/**
 * This Controller handles the redirections and logic of each front's URL, but the front templates which is not React.  
 * @author KurixDeCuatroCuernos
 * @version 0.1.0
 */
@Controller
public class MainController {
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private ObraRepository obraRepo;
	
	@Autowired
	private ComentarioRepository commentRepo;
	
	@Autowired
	private ObraService obraService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private ComentarioService commentService;
	
	@Autowired
	private AuthenticationManager authManager;
	
	@Autowired
	private BCryptPasswordEncoder encoder;
	
	@Autowired
	private HttpServletRequest request;
	
	/**
	 * This method handles the logic for the "/" URL, sets the user-related data and work suggestions 
	 * in the Model, and redirects to the "index.html" template.
	 * <p>
	 * It performs the following tasks:
	 * <ul>
	 *   <li>If the user is authenticated, it adds the username and user ID to the model.</li>
	 *   <li>If the user is not authenticated, it adds "invitado" as the username in the model.</li>
	 *   <li>It retrieves a list of suggested works (Obra) with ISBN and title and adds them to the model.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param model The Model object to hold attributes to be sent to the view (frontend).
	 * @return The "index.html" template to be rendered, containing the data to be displayed on the home page.
	 */
	@GetMapping("/")
	public String index(Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		System.out.println("Estoy en la página principal");
		if (auth != null && auth.isAuthenticated()) {
			model.addAttribute("username", auth.getName());
			model.addAttribute("userId", auth.getPrincipal());
		} else {
			model.addAttribute("username", "invitado");
		}
		List<ObraIsbnTituloProjection> suggestions = obraRepo.findAllIsbnAndTitulo();
		System.out.println("sugerencias: "+suggestions.toString());
		
		model.addAttribute("suggestWorks",suggestions);
		
		return "/index";
	}
	
	/**
	 * This method handles the logic for the "/workShow" URL, retrieves the work (Obra) by its ID, 
	 * sets the data in the Model, and then redirects to the appropriate template.
	 * <p>
	 * The behavior is as follows:
	 * <ul>
	 *   <li>If <code>id</code> is <code>null</code>, it redirects to the home page ("/").</li>
	 *   <li>If no work (Obra) is found for the given <code>id</code>, it redirects to the home page ("/").</li>
	 *   <li>If a work is found and the <code>id</code> is valid, it returns the "/workShow?id" template.</li>
	 * </ul>
	 * </p>
	 *
	 * @param id The ID (ISBN) of the Obra object to be shown. It is a Long type.
	 * @param model The Model to hold attributes that will be sent to the view (frontend).
	 * @return The view template to be rendered:
	 *         <ul>
	 *           <li>"/" if <code>id</code> is <code>null</code> or no work is found.</li>
	 *           <li>"/workShow?id" if the work is found and the <code>id</code> is valid.</li>
	 *         </ul>
	 */
	@GetMapping("/workShow")
	public String mostrarObra(@RequestParam() Long id, Model model) {
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    Usuario user = new Usuario();
	    
	    // Check if user is authenticated and set the user details accordingly
	    if (auth != null && auth.isAuthenticated()) {
	        model.addAttribute("username", auth.getName());
	        if (auth.getName().equals("anonymousUser")) {
	            model.addAttribute("user", user);
	        } else {
	            user = (Usuario) auth.getPrincipal();
	            model.addAttribute("user", auth.getPrincipal());
	        }
	    } else {
	        user = new Usuario();
	        user.setId("invitado");
	        model.addAttribute("username", "invitado");
	        model.addAttribute("user", user);
	    }

	    String retorno = "";

	    // If the id is provided, attempt to fetch the Obra object
	    if (id != null) {
	        Optional<Obra> obra = obraRepo.findById(id);
	        
	        if (obra.isPresent()) {
	            // Set the suggested works and comments in the model
	            List<ObraIsbnTituloProjection> suggestions = obraRepo.findAllIsbnAndTitulo();
	            model.addAttribute("suggestWorks", suggestions);
	            model.addAttribute("obra", obra.get());

	            // Fetch and sort comments by date (descending)
	            Sort sortByDate = Sort.by(Order.desc("fecha"));
	            List<Comentario> comments = commentRepo.findAllByObraAndTipo(obra.get().getIsbn(), "COMMENT", sortByDate);
	            model.addAttribute("comments", comments);

	            // Prepare painted comments with user info
	            List<ComentarioPintado> paintcomments = new ArrayList<>();
	            Optional<UsuarioComentarioPintado> userToPaint;
	            if (!comments.isEmpty()) {
	                for (Comentario comment : comments) {
	                    userToPaint = userRepo.findByIdOnlyIdAndNameAndRole(comment.getUsuario());
	                    if (userToPaint.isPresent()) {
	                        paintcomments.add(new ComentarioPintado(comment, userToPaint.get().id(), userToPaint.get().name(), userToPaint.get().role()));
	                    }
	                }
	                model.addAttribute("comments", paintcomments);
	            } else {
	                System.out.println("No se encontraron comentarios para esa obra");
	            }

	            // Fetch and sort answers by date (ascending)
	            sortByDate = Sort.by(Order.asc("fecha"));
	            List<ComentarioPintado> paintanswers = new ArrayList<>();
	            List<Comentario> answers = commentRepo.findAllByObraAndTipo(obra.get().getIsbn(), "ANSWER", sortByDate);
	            if (!answers.isEmpty()) {
	                for (Comentario answer : answers) {
	                    userToPaint = userRepo.findByIdOnlyIdAndNameAndRole(answer.getUsuario());
	                    if (userToPaint.isPresent()) {
	                        paintanswers.add(new ComentarioPintado(answer, userToPaint.get().id(), userToPaint.get().name(), userToPaint.get().role()));
	                    }
	                }
	                model.addAttribute("answers", paintanswers);
	            } else {
	                System.out.println("No se encontraron respuestas para esa obra");
	            }

	            // Check if the user has already commented on the obra
	            boolean comentado = false;
	            for (Comentario comment : comments) {
	                if (comment.getUsuario().equals(user.getId())) {
	                    comentado = true;
	                }
	            }
	            model.addAttribute("comentar", comentado);

	            retorno = "workShow";        
	        } else {
	            System.out.println("No se ha encontrado la obra con isbn: " + id);
	            retorno = "/";
	        }
	    } else {
	        System.out.println("No se ha recibido id");
	        retorno = "/";
	    }

	    return retorno;
	}
	
	/**
	 * This method handles the submission of a comment for a work (Obra) and saves it to the database.
	 * <p>
	 * The following steps are performed:
	 * <ul>
	 *   <li>A new comment is created with the provided details (title, text, rating, etc.) and saved to the repository.</li>
	 *   <li>If the comment is successfully saved, the method redirects the user to the work details page ("/workShow?id={obraId}").</li>
	 *   <li>If an error occurs during the comment submission, an error message is returned with a 500 status code.</li>
	 * </ul>
	 * </p>
	 *
	 * @param obraId The ID of the work (Obra) to which the comment belongs.
	 * @param userId The ID of the user submitting the comment.
	 * @param titulo The title of the comment.
	 * @param texto The text content of the comment.
	 * @param valoracion The rating value given to the work (e.g., a scale of 1 to 5).
	 * @return A ResponseEntity containing the URL of the work's details page if the comment was successfully saved, 
	 *         or an error message with a 500 status code if there was an issue saving the comment.
	 */
	@PostMapping("/workComment/submit")
	public ResponseEntity<String> comentarObra(@RequestParam Long obraId, @RequestParam String userId, @RequestParam String titulo, @RequestParam String texto, @RequestParam int valoracion) {

	    String retorno = "";
	    try {
	    	
	        Comentario comment = new Comentario(null, titulo, texto, LocalDateTime.now(), valoracion, "COMMENT", obraId, userId);
	        commentRepo.save(comment);

	        // en este caso no recargamos la página para poder hacerlo en ella y mandar una confirmación
	        retorno = "/workShow?id=" + obraId;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar el comentario.");
	    }

	    return ResponseEntity.ok(retorno);
	}
	
	/**
	 * This method handles the deletion of a comment and its associated responses from the database.
	 * <p>
	 * The following actions are performed:
	 * <ul>
	 *   <li>Attempts to find and delete any responses associated with the comment.</li>
	 *   <li>Deletes the comment itself from the repository.</li>
	 *   <li>If successful, redirects the user back to the work details page ("/workShow?id={obraId}").</li>
	 *   <li>If an error occurs during the deletion process, redirects the user to an error page ("/errorPage").</li>
	 * </ul>
	 * </p>
	 *
	 * @param id The ID of the comment to be deleted.
	 * @param obraId The ID of the work (Obra) associated with the comment, used for redirection after deletion.
	 * @return A string indicating the URL to redirect to:
	 *         <ul>
	 *           <li>Redirects to the work details page ("/workShow?id={obraId}") if the deletion is successful.</li>
	 *           <li>Redirects to the error page ("/errorPage") if there is an exception during the process.</li>
	 *         </ul>
	 */
	@GetMapping("/workShow/deleteComment")
	public String borrarComentario(@RequestParam String id, @RequestParam String obraId) {
		String retorno ="redirect:/workShow?id="+obraId;
		
		try {
			List<Comentario> answers = commentRepo.findAllByComment(id);
			if(!answers.isEmpty()) {
				System.out.println("Respuestas asociadas borradas");
				commentRepo.deleteAll(answers);
			} else {
				System.out.println("No se encontraron respuestas asociadas que borrar");
			}
			commentRepo.deleteById(id);
			
		} catch (Exception e) {
			e.printStackTrace();
			retorno="/errorPage";
		}
		
		return retorno;
	}
	
	/**
	 * This method handles the submission of a reply to an existing comment on a work (Obra).
	 * It saves the reply in the database and then redirects the user to the work details page.
	 * <p>
	 * The following actions are performed:
	 * <ul>
	 *   <li>A new reply (comment of type "ANSWER") is created and saved to the repository with the provided details.</li>
	 *   <li>If the reply is successfully saved, the user is redirected to the work details page ("/workShow?id={obraId}").</li>
	 *   <li>If an error occurs during the saving process, a 500 error response is returned with an error message.</li>
	 * </ul>
	 * </p>
	 *
	 * @param obraId The ID of the work (Obra) to which the comment belongs.
	 * @param userId The ID of the user submitting the reply.
	 * @param commentId The ID of the comment being replied to.
	 * @param texto The text content of the reply.
	 * @return A ResponseEntity containing:
	 *         <ul>
	 *           <li>The URL of the work details page ("/workShow?id={obraId}") if the reply was successfully saved.</li>
	 *           <li>An error message with a 500 status code if there was an issue saving the reply.</li>
	 *         </ul>
	 */
	@PostMapping("/answerComment/submit")
	public ResponseEntity<String> responderComentario(@RequestParam Long obraId, @RequestParam String userId, @RequestParam String commentId, @RequestParam String texto) {
		String retorno = "/workShow?id="+obraId;
		System.out.println("obra: "+obraId+" userId: "+userId+" commentId: "+" texto: "+texto);
		
		try {
			/*String id, String texto, LocalDateTime fecha, String tipo, Long obra,
			String usuario, String comment)*/
			Comentario comment = new Comentario (null, texto, LocalDateTime.now(), "ANSWER", obraId, userId, commentId);
			commentRepo.save(comment);
		} catch (Exception e){
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar el comentario.");
		}
		
		
		return ResponseEntity.ok(retorno);
		
	}
	
	/**
	 * This method handles the retrieval of a comment for editing purposes.
	 * It fetches a specific comment by its ID from the repository and returns it as a JSON response.
	 * <p>
	 * The following actions are performed:
	 * <ul>
	 *   <li>Attempts to find the comment using the provided <code>commentId</>.</li>
	 *   <li>If the comment is found, it is returned as a JSON response with a 200 OK status.</li>
	 *   <li>If the comment is not found, a 404 Not Found response is returned.</li>
	 *   <li>If an error occurs during the process, a 500 Internal Server Error response is returned.</li>
	 * </ul>
	 * </p>
	 *
	 * @param obraId The ID of the work (Obra) to which the comment belongs. This is used for context but is not directly used in the retrieval.
	 * @param commentId The ID of the comment to be retrieved for editing.
	 * @return A ResponseEntity containing:
	 *         <ul>
	 *           <li>The requested comment in JSON format with a 200 OK status if the comment is found.</li>
	 *           <li>A 404 Not Found status with a null body if the comment does not exist.</li>
	 *           <li>A 500 Internal Server Error status with a null body if an exception occurs during the process.</li>
	 *         </ul>
	 */
	@GetMapping("/editComment")
	@ResponseBody
	public ResponseEntity<Comentario> editarComentario(@RequestParam Long obraId, @RequestParam String commentId) {
	    Optional<Comentario> comment;
	    try {
	        comment = commentRepo.findById(commentId);
	        if (comment.isPresent()) {
	            return ResponseEntity.ok(comment.get()); // Devuelve el comentario como JSON
	        } else {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Si no se encuentra el comentario
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Si hay un error
	    }
	}
	
	/**
	 * This method handles the updating of an existing comment. It allows the user to modify the title, text, 
	 * and rating of a comment. After updating, the comment is saved in the repository.
	 * <p>
	 * The following actions are performed:
	 * <ul>
	 *   <li>Attempts to find the comment by its ID.</li>
	 *   <li>If the comment is found, its fields (title, text, rating) are updated with the provided values.</li>
	 *   <li>The updated comment is saved back to the repository.</li>
	 *   <li>If the update is successful, a success message is returned with a 200 OK status.</li>
	 *   <li>If the comment cannot be found or an error occurs during the process, an error message is returned with a 500 Internal Server Error status.</li>
	 * </ul>
	 * </p>
	 *
	 * @param commentId The ID of the comment to be updated.
	 * @param titulo The new title to set for the comment.
	 * @param texto The new text content to set for the comment.
	 * @param valoracion The new rating value to set for the comment.
	 * @return A ResponseEntity containing:
	 *         <ul>
	 *           <li>A success message ("Comentario actualizado") with a 200 OK status if the update is successful.</li>
	 *           <li>An error message ("Error al actualizar el comentario") with a 500 Internal Server Error status if an error occurs.</li>
	 *         </ul>
	 */
	@PostMapping("/updateComment")
	@ResponseBody
	public ResponseEntity<String> updateComment(@RequestParam String commentId, @RequestParam String titulo, @RequestParam String texto, @RequestParam int valoracion) {
	    ResponseEntity<String> retorno = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar el comentario");
		try {
	    	System.out.println("actualizando comentario");
	        Optional<Comentario> oldComment = commentRepo.findById(commentId);
	        if (oldComment.isPresent()) {
	        	Comentario newComment = oldComment.get();
	        	try {
	        		newComment.setTitulo(titulo);
	        		newComment.setTexto(texto);
	        		newComment.setValoracion(valoracion);
	        		commentRepo.save(newComment); 
	        	} catch (Exception e) {
	        		e.printStackTrace();
	        	} 
	            retorno = ResponseEntity.ok("Comentario actualizado");
	        } else {
	        	System.out.println("no se puede actualizar el comentario");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        retorno = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar el comentario");
	    }
		return retorno;
	}
	
	/**
	 * This method handles the login page redirection logic. It checks if the user is already authenticated.
	 * <p>
	 * The following actions are performed:
	 * <ul>
	 *   <li>If the user is authenticated and not an anonymous user, they are redirected to the homepage ("/").</li>
	 *   <li>If the user is not authenticated, they are redirected to the login page ("/loginPage").</li>
	 * </ul>
	 * </p>
	 *
	 * @param model The Model object that may be used to pass attributes to the view (not used in this implementation).
	 * @return A string representing the view name or redirection URL:
	 *         <ul>
	 *           <li>"redirect:/" if the user is authenticated.</li>
	 *           <li>"/loginPage" if the user is not authenticated.</li>
	 *         </ul>
	 */
	@GetMapping("/login")
	public String login(Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String retorno ="";
		if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
			retorno= "redirect:/";
		} else {
			retorno = "/loginPage";
		}
		return retorno;
	}
	
	/**
	 * This method handles the login submission process. It attempts to authenticate the user based on the provided
	 * credentials (email and password). If authentication is successful, the user is redirected to the homepage.
	 * If authentication fails, an error message is shown, and the user is redirected to a login error page.
	 * <p>
	 * The following actions are performed:
	 * <ul>
	 *   <li>Attempts to authenticate the user using the provided email and password.</li>
	 *   <li>If authentication is successful, the user's authentication details are stored in the security context.</li>
	 *   <li>If authentication fails, an error message is set, and the user is redirected to the login error page.</li>
	 * </ul>
	 * </p>
	 *
	 * @param logUser The user object containing the email and password provided by the user for login.
	 * @param model The Model object used to pass attributes to the view (not used in this implementation).
	 * @param redirectAttributes Used to add flash attributes for redirection, especially for error messages.
	 * @return A string representing the view name or redirection URL:
	 *         <ul>
	 *           <li>"redirect:/" if the user is successfully authenticated.</li>
	 *           <li>"redirect:/loginError" if authentication fails.</li>
	 *         </ul>
	 */
	@PostMapping("/loginPage/submit")
	public String loginSubmit(@Valid @ModelAttribute Usuario logUser, Model model, RedirectAttributes redirectAttributes) {
		String retorno ="redirect:/";
		try {
            System.out.println("Intentando autenticar...");
			UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(logUser.getEmail(), logUser.getPassword());
			System.out.println("Usuario insertado en BD: "+logUser.getEmail()+" "+logUser.getPassword());
            Authentication authentication = authManager.authenticate(authToken);
            // Establecer la autenticación en el contexto de seguridad
            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("Autenticado");
            
            // Asegurarse de que la sesión HTTP almacena la autenticación
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Hubo error al autenticar");
            redirectAttributes.addFlashAttribute("message", "Registration successful, but login failed.");
            retorno = "redirect:/loginError";
        }
		return retorno;
	}
	
	/**
	 * This method handles the login error page. It is invoked when a login attempt fails, allowing the user
	 * to be redirected to a page that informs them of the failure.
	 * <p>
	 * This method typically shows an error message indicating that the login attempt was unsuccessful, 
	 * and it might be used to display additional error details or guidance for the user.
	 * </p>
	 *
	 * @param modelo The Model object used to pass attributes to the view (not used in this implementation).
	 * @return A string representing the view name:
	 *         <ul>
	 *           <li>"/loginError" to display the login error page.</li>
	 *         </ul>
	 */
	@GetMapping("/loginError")
	public String loginError(Model modelo) {
		return "/loginError";
	}
	
	/**
	 * This method handles the logic for displaying the users list page. It checks if the current user is authenticated
	 * and has an "ADMIN" role. If the user is an admin, it fetches the list of all users and displays it. Otherwise,
	 * the user is redirected to the homepage.
	 * <p>
	 * The following actions are performed:
	 * <ul>
	 *   <li>If the user is authenticated, their username is added to the model.</li>
	 *   <li>If the user has the "ADMIN" role, the list of all users is retrieved from the repository and added to the model.</li>
	 *   <li>If the user is not an admin, they are redirected to the homepage.</li>
	 * </ul>
	 * </p>
	 *
	 * @param model The Model object used to pass attributes to the view, such as the username and the list of users.
	 * @return A string representing the view name:
	 *         <ul>
	 *           <li>"/usersList" if the current user is an admin, showing the list of users.</li>
	 *           <li>"/" if the current user is not an admin, redirecting to the homepage.</li>
	 *         </ul>
	 */
	@GetMapping("/usersList")
	public String listado(Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		System.out.println("Estoy en la página principal");
		if (auth != null && auth.isAuthenticated()) {
			model.addAttribute("username", auth.getName());
		} else {
			model.addAttribute("username", "invitado");
		}
		String retorno = "";
		Usuario usuario = getCurrentUsuario();
		if (usuario != null && "ADMIN".equals(usuario.getRole())) {
			retorno = "/usersList";
			model.addAttribute("listaUsuarios", userRepo.findAll());
		} else {
			System.out.println("El usuario no es administrador");
			retorno = "/";
		}
		return retorno;
	}
	
	/**
	 * This method handles the display of the registration form page. It adds an attribute to the model, which can 
	 * be used for editing a user (in this case, it is set to null to indicate no existing user to edit).
	 * <p>
	 * The following actions are performed:
	 * <ul>
	 *   <li>The method adds a "usuarioEdit" attribute to the model, initialized to null, indicating that no user 
	 *       is being edited at this time.</li>
	 *   <li>The method returns the view name for the registration form page.</li>
	 * </ul>
	 * </p>
	 *
	 * @param model The Model object used to pass attributes to the view, such as the "usuarioEdit" attribute.
	 * @return A string representing the view name:
	 *         <ul>
	 *           <li>"/form" to display the registration form page.</li>
	 *         </ul>
	 */
	@GetMapping("form")
	public String registro(Model model) {
		model.addAttribute("usuarioEdit", null);
		return "/form";
	}
	
	/**
	 * This method handles the form submission for user registration. It validates the submitted user data, 
	 * checks for errors, and attempts to register a new user. If any errors are found, it redirects the user
	 * back to the registration form with error messages. If registration is successful, the user is authenticated 
	 * and logged in immediately, redirecting them to the home page.
	 * <p>
	 * The method performs the following actions:
	 * <ul>
	 *   <li>Validates the user input (e.g., username, birthdate, email, password).</li>
	 *   <li>If any errors are found in the submitted data, it adds error messages to be displayed and redirects 
	 *       the user back to the registration page.</li>
	 *   <li>If validation passes, the user is saved to the repository with the role "STUDENT" and their password 
	 *       is encoded.</li>
	 *   <li>The user is immediately authenticated and logged in.</li>
	 *   <li>If the authentication fails, an error message is displayed, and the user is redirected to the login page.</li>
	 * </ul>
	 * </p>
	 *
	 * @param nuevoUsuario The user object containing the form data to be registered.
	 * @param model The Model object used to pass attributes to the view (not used in this method).
	 * @param br The BindingResult object that contains validation errors, if any.
	 * @param redirectAttributes The RedirectAttributes object used to pass flash attributes (such as error messages)
	 *                           between redirects.
	 * @return A string representing the redirect location:
	 *         <ul>
	 *           <li>"redirect:/form" if there are validation errors, redirecting back to the registration form.</li>
	 *           <li>"redirect:/" if registration and authentication are successful, redirecting to the homepage.</li>
	 *           <li>"redirect:/login" if authentication fails after registration, redirecting to the login page.</li>
	 *         </ul>
	 */
	@PostMapping("/form/submit")
	public String handleFormSubmit(@Valid @ModelAttribute("userForm") Usuario nuevoUsuario, Model model, BindingResult br, RedirectAttributes redirectAttributes) {
		String retorno="";
		String mensaje="";
		boolean check=true;
		
		if (br.hasErrors()) {
			br.getAllErrors().forEach(error -> System.out.println(error.toString()));
			check=false;
			mensaje+="Errors in register\n";
		} 
		if (nuevoUsuario.getName()==null) {
			check=false;
			mensaje+="You need an username";
		}
		if (nuevoUsuario.getFechaNac()==null) {
			check=false;
			mensaje+="You need a birthday date\n";
		}
		if (nuevoUsuario.getEmail()==null || !nuevoUsuario.getEmail().contains("@")) {
			check=false;
			mensaje+="You need a valid email\n";
		}
		
		if (check==false) {
			redirectAttributes.addFlashAttribute("message", mensaje);  // Usar addFlashAttribute
			retorno = "redirect:/form";  // Redirigir a la página de registro
		} else {
			nuevoUsuario.setRole("STUDENT");
			String rawPassword = nuevoUsuario.getPassword();
			nuevoUsuario.setPassword(encoder.encode(rawPassword));
			userRepo.save(nuevoUsuario);
			System.out.println(nuevoUsuario.getEmail());
			
			try {
	            System.out.println("Intentando autenticar...");
				UsernamePasswordAuthenticationToken authToken = 
	                new UsernamePasswordAuthenticationToken(nuevoUsuario.getEmail(), rawPassword);
				System.out.println("Usuario insertado en BD: "+nuevoUsuario.getEmail()+" "+nuevoUsuario.getPassword());
	            System.out.println("Usuario para inicio de sesión: "+authToken.getName()+" "+authToken.getCredentials()+" "+authToken.getDetails());
	            Authentication authentication = authManager.authenticate(authToken);
	            // Establecer la autenticación en el contexto de seguridad
	            SecurityContextHolder.getContext().setAuthentication(authentication);
	            System.out.println("Autenticado");
	            
	         // Guardar el contexto de seguridad en la sesión
	            HttpSession session = request.getSession(true);
	            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

	            
	            retorno="redirect:/";
	        } catch (Exception e) {
	            e.printStackTrace();
	            System.out.println("Hubo error al autenticar");
	            redirectAttributes.addFlashAttribute("message", "Registration successful, but login failed.");
	            retorno = "redirect:/login";
	        }

		}
		return retorno;
	}
	
	/**
	 * This method handles the request to display the user edit form. It retrieves the user by their ID from the database
	 * and, if found, populates the form with the user's existing data. If the user is not found, the method redirects 
	 * to the registration form.
	 * <p>
	 * The following actions are performed:
	 * <ul>
	 *   <li>The method retrieves a user from the repository using the provided user ID.</li>
	 *   <li>If the user is found, their details are added to the model and the user is directed to the form for editing.</li>
	 *   <li>If the user is not found, the method redirects the user to the registration form page.</li>
	 * </ul>
	 * </p>
	 *
	 * @param id The ID of the user to be edited. This is extracted from the URL path.
	 * @param model The Model object used to pass attributes to the view. It is used to add the user data (if found) to the form.
	 * @return A string representing the view name:
	 *         <ul>
	 *           <li>"form" if the user is found, redirecting to the user edit form with the user's data.</li>
	 *           <li>"redirect:/form" if the user is not found, redirecting to the registration form.</li>
	 *         </ul>
	 */
	@GetMapping("/edit{id}")
	public String editarUsuarioForm(@PathVariable("id") String id, Model model) {
		
		String retorno="";
		Optional<Usuario> user = userRepo.findById(id);
		System.out.println("recogiendo el usuario"+ user.get().toString());
		if (user.isPresent()) {
			model.addAttribute("usuarioEdit", user.get());
			retorno = "form";
		} else {
			retorno = "redirect:/form";
		}
		return retorno;
	}
	
	/**
	 * This method handles the form submission for updating a user's details. It validates the user input 
	 * and, if no errors are found, it saves the updated user information. If there are validation errors, 
	 * the method returns the user to the form for editing.
	 * <p>
	 * The following actions are performed:
	 * <ul>
	 *   <li>The method receives the updated user data from the form.</li>
	 *   <li>It validates the input using the BindingResult. If there are validation errors, the user is 
	 *       returned to the form to fix the errors.</li>
	 *   <li>If no validation errors are found, the method updates the user's information by calling the 
	 *       edit method of the userService, and then redirects the user to the user list page.</li>
	 * </ul>
	 * </p>
	 *
	 * @param editarUsuario The updated user details submitted from the form.
	 * @param br The BindingResult that contains any validation errors. It is used to check if the user input is valid.
	 * @return A string representing the view name:
	 *         <ul>
	 *           <li>"form" if there are validation errors, returning the user to the form to fix the errors.</li>
	 *           <li>"redirect:/usersList" if the user is successfully updated, redirecting to the list of users.</li>
	 *         </ul>
	 */
	@PostMapping("/edit/submit")
	public String editarUsuarioSubmit(@Valid @ModelAttribute("userForm") Usuario editarUsuario, BindingResult br) {
		String retorno = "";
		System.out.println("Modificando: "+editarUsuario.toString());
		if (br.hasErrors()){
			retorno = "form";
		} else {
			userService.edit(editarUsuario);
			retorno="redirect:/usersList";
		}
		
		return retorno;
	}
	
	/**
	 * This method handles the deletion of a user and their associated comments. It finds the user by their ID, 
	 * deletes all comments associated with the user, and then deletes the user from the database.
	 * <p>
	 * The following actions are performed:
	 * <ul>
	 *   <li>The method attempts to find the user using the provided user ID.</li>
	 *   <li>If the user is found, all comments associated with that user are deleted from the comment repository.</li>
	 *   <li>The user is then deleted from the user repository.</li>
	 *   <li>Finally, the method redirects to the user list page after the deletion process is complete.</li>
	 * </ul>
	 * </p>
	 *
	 * @param id The ID of the user to be deleted. This ID is provided as a request parameter.
	 * @param model The Model object used to pass attributes to the view. In this case, it's not used directly but is part of the method signature.
	 * @return A string representing the view name, which is a redirect to the users list page after the deletion is completed.
	 */
	@GetMapping("/userlist/delete")
	public String deleteUser(@RequestParam String id, Model model) {
		Optional<Usuario> user = userRepo.findById(id);
		if (user.isPresent()) {
			System.out.println("Se ha borrado al usuario: ");
			System.out.println(user.get().getId().toString()+" y los comentarios y asociados");
			List<Comentario> commentsDelete = commentRepo.findAllByUsuario(id);
			for (Comentario comment : commentsDelete) {
				commentRepo.deleteById(comment.getId());
			}
			userRepo.deleteById(user.get().getId());
		} 
		return "redirect:/usersList";
		
	}
	
	/**
	 * This method retrieves the currently authenticated user from the security context.
	 * <p>
	 * It checks the authentication context to find the principal object. If the principal is an instance of the 
	 * {@link Usuario} class, it returns the authenticated user. If the user is not authenticated or the principal 
	 * is not a {@link Usuario}, it returns null.
	 * </p>
	 *
	 * @return The currently authenticated user, or null if no user is authenticated or if the authenticated 
	 *         principal is not an instance of {@link Usuario}.
	 */
	public Usuario getCurrentUsuario() {
        Usuario retorno=null;
        
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Usuario) {
            retorno = (Usuario) authentication.getPrincipal();
        }
        return retorno;
    }
	
	/**
	 * This method handles the logic for displaying the list of works (obras). It checks if the currently authenticated user
	 * has an "ADMIN" role. If the user is an administrator, it retrieves and displays all works from the database.
	 * If the user is not an administrator, they are redirected to the home page.
	 * 
	 * <p>
	 * The following actions are performed:
	 * <ul>
	 *   <li>The method checks if the currently authenticated user has an "ADMIN" role.</li>
	 *   <li>If the user is an administrator, it retrieves all works from the repository and adds them to the model.</li>
	 *   <li>If the user is not an administrator, the method redirects to the home page.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param model The model object used to pass attributes to the view. In this case, it holds the list of works
	 *              to be displayed on the page.
	 * @return A string representing the view name. If the user is an admin, it returns "/workList"; otherwise, it 
	 *         redirects to the home page with "/".
	 */
	@GetMapping("/workList")
	public String listarObras(Model model) {
		String retorno = "";
		Usuario usuario = getCurrentUsuario();
		if (usuario != null && "ADMIN".equals(usuario.getRole())) {
			retorno = "/workList";
			model.addAttribute("obraList", obraRepo.findAll());
		} else {
			System.out.println("El usuario no es administrador");
			retorno = "/";
		}
		return retorno;
	}
	
	/**
	 * This method handles the logic for displaying the form to insert a new book (libro). It optionally accepts a 
	 * "failure" parameter to indicate if there was an error during the process and displays an appropriate message 
	 * on the form. It also retrieves all distinct tags (temas) related to the works (obras) and provides them to 
	 * the view for selection. 
	 * 
	 * <p>
	 * The following actions are performed:
	 * <ul>
	 *   <li>If the "failure" parameter is present, it adds it as an attribute to the model to display an error message.</li>
	 *   <li>It retrieves all distinct tags (temas) from the `obraService` and adds them to the model in JSON format.</li>
	 *   <li>It sets the type of the work to "BOOK" in the model to be used in the form.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param failure An optional parameter indicating if there was a failure during the process. If present, an error
	 *                message will be displayed on the form.
	 * @param model The model object used to pass attributes to the view. In this case, it contains the list of tags
	 *              and other attributes required for the form.
	 * @return The name of the view ("workForm"), which will display the form to insert a new book.
	 */
	@GetMapping("/workForm/book")
	public String insertarLibro(@RequestParam(required=false) String failure, Model model) {
		if (failure != null) {
			model.addAttribute("failure",failure);
		}
		
		List<String> tags = obraService.findAllDifferentTemas();
		System.out.println("Tags recogidos: "+tags.toString());

		model.addAttribute("tagsJson", tags);		
		model.addAttribute("type","BOOK");
		return "workForm";
	}
	
	/**
	 * This method handles the submission of the form for inserting a new book (libro) into the system. It performs validation 
	 * checks and handles the insertion process into the database. If there are validation errors or if the book already exists, 
	 * it redirects back to the form with a failure message. If the insertion is successful, it redirects to the work list page.
	 * 
	 * <p>
	 * The following steps are performed:
	 * <ul>
	 *   <li>Validates the form using the provided `BindingResult` to check for errors.</li>
	 *   <li>Checks if a book with the same ISBN already exists in the database.</li>
	 *   <li>If errors are found or if the book already exists, the user is redirected to the book insertion form with an error message.</li>
	 *   <li>If the book does not exist and there are no validation errors, the book is inserted into the database with the given tags.</li>
	 *   <li>The tags provided as a comma-separated string are converted into a list and added to the book object.</li>
	 *   <li>The book's type is set to "BOOK" and the book is saved in the repository.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param insertarObra The `Obra` object containing the information for the new book (book details, ISBN, etc.).
	 * @param tags A comma-separated string of tags (temas) related to the book, which will be converted into a list.
	 * @param br The `BindingResult` object used to store validation errors (if any) from the form submission.
	 * @return A string indicating where to redirect after the form submission:
	 *         - If errors are found or if the book already exists, the user is redirected back to the book form with an error message.
	 *         - If successful, the user is redirected to the work list page.
	 */
	@PostMapping("/workForm/bookSubmit")
	public String insertarLibroSubmit(@Valid @ModelAttribute("WorkForm") Obra insertarObra,@RequestParam("tags") String tags, BindingResult br) {
		String retorno = "";
		
		Optional<Obra> test = obraRepo.findByIsbn(insertarObra.getIsbn());
		if (br.hasErrors() || test.isPresent()){
			// This could use to give mor information to the user
			retorno = "redirect:/workForm/book?failure=Problems to insert the new book or it exist";
		} else {
			// converts the string in a List of strings separated by comas and insert in the object
			List<String> tagsList = Arrays.asList(tags.split(","));
			insertarObra.setTemas(tagsList);
			
			System.out.println("Insertando "+insertarObra.toString());
			insertarObra.setTipo("BOOK");
			obraRepo.save(insertarObra);
			retorno="redirect:/workList";
		}
		
		return retorno;
	}
	
	/**
	 * This method handles the display of the article creation form. It retrieves a list of available tags (temas) for the article 
	 * and adds them to the model. If a failure message is provided (such as an error from the form submission), it adds that message 
	 * to the model. The method prepares the form for creating a new article by setting the necessary attributes in the model, 
	 * including the available tags and the type of work (ARTICLE).
	 * 
	 * <p>
	 * The following steps are performed:
	 * <ul>
	 *   <li>If a failure message is provided (via the "failure" request parameter), it is added to the model.</li>
	 *   <li>The method retrieves a list of different available tags (temas) through the `obraService`.</li>
	 *   <li>The tags are added to the model as a JSON object for use in the form.</li>
	 *   <li>The type of work ("ARTICLE") is set and added to the model, which indicates the type of the object being created.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param failure An optional query parameter that contains a failure message if the form submission failed. Can be null.
	 * @param model The `Model` object used to pass data to the view (form) for rendering.
	 * @return The name of the view (`"workForm"`), which is the article creation form.
	 */
	@GetMapping("/workForm/article")
	public String insertarArticulo(@RequestParam(required=false) String failure, Model model) {
		if (failure != null) {
			model.addAttribute("failure",failure);
		}
		
		List<String> tags = obraService.findAllDifferentTemas();
		System.out.println("Tags recogidos: "+tags.toString());

		model.addAttribute("tagsJson", tags);
		model.addAttribute("type","ARTICLE");
		return "workForm";
	}
	
	/**
	 * This method handles the submission of the article creation form. It performs validation on the input data 
	 * and attempts to insert a new article (of type "ARTICLE") into the database.
	 * 
	 * <p>
	 * The following steps are performed:
	 * <ul>
	 *   <li>If validation errors are present or if the ISBN of the article already exists in the database, the method redirects the user 
	 *       back to the form with a failure message.</li>
	 *   <li>If validation passes and the ISBN is not already in use, the method proceeds by converting the tags (provided as a comma-separated string) 
	 *       into a list of strings and sets them as themes (temas) for the article.</li>
	 *   <li>The type of work is set to "ARTICLE", and the article object is saved to the repository.</li>
	 *   <li>If the insertion is successful, the user is redirected to the list of works.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param insertarObra The article object to be inserted, populated with the data submitted through the form.
	 * @param tags A comma-separated string of tags (temas) to be associated with the article.
	 * @param br The binding result that holds any validation errors from the form submission.
	 * @return A redirection URL. If there are errors or if the ISBN already exists, the method redirects back to the form. 
	 *         Otherwise, it redirects to the list of works.
	 */
	@PostMapping("/workForm/articleSubmit")
	public String insertarArticuloSubmit(@Valid @ModelAttribute("WorkForm") Obra insertarObra,@RequestParam("tags") String tags, BindingResult br) {
		String retorno = "";
		
		Optional<Obra> test = obraRepo.findByIsbn(insertarObra.getIsbn());
		if (br.hasErrors() || test.isPresent()){
			// This could use to bring mor information to the user
			retorno = "redirect:/workForm/book?failure=Problems to insert the new book or it exist";
		} else {
			
			// converts the string in a List of strings separated by comas and insert in the object
			List<String> tagsList = Arrays.asList(tags.split(","));
			insertarObra.setTemas(tagsList);
			
			System.out.println("Insertando "+insertarObra.toString());
			insertarObra.setTipo("ARTICLE");
			obraRepo.save(insertarObra);
			retorno="redirect:/workList";
		}
		
		return retorno;
	}
		
	/**
	 * This method handles the deletion of a work (book or article) from the system.
	 * It first checks if the work exists in the database by its ID. If it does, it proceeds to delete 
	 * the work along with all associated comments and their replies to avoid orphan entries in the database.
	 * 
	 * <p>
	 * The following steps are performed:
	 * <ul>
	 *   <li>Check if the work exists in the database by its ID.</li>
	 *   <li>If the work exists, it retrieves all comments associated with the work using the work ID.</li>
	 *   <li>Deletes each comment and its responses to prevent orphaned records.</li>
	 *   <li>Deletes the work from the database using its ISBN to ensure the correct entry is removed.</li>
	 *   <li>If the work is successfully deleted, the user is redirected to the list of works.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param id The ID of the work to be deleted from the database.
	 * @return A redirection URL to the list of works. After the work and its associated comments are deleted, 
	 *         the user is redirected to the "workList" page.
	 */
	@GetMapping("/workList/delete{id}")
	public String borrarObra(Long id) {
		Optional<Obra> obra = obraRepo.findById(id);
		if (obra.isPresent()) {
			System.out.println("Se ha borrado la obra: ");
			System.out.println(obra.get().getIsbn()+obra.get().getTitulo()+"\n y los comentarios y respuestas asociados a ella");
			
			List<Comentario> commentsDelete = commentRepo.findAllByObra(id); // recogemos todos los comentarios y respuestas de esa obra para borrarlos y evitar entradas fantasma
			for (Comentario comment : commentsDelete) {
				commentRepo.deleteById(comment.getId());
			}
			obraRepo.deleteByIsbn(obra.get().getIsbn());
		} 
		return "redirect:/workList";
	}
	
	/**
	 * This method handles the redirection logic for editing a work (book or article) in the system.
	 * It checks if the work with the given ID exists in the database. If the work is found,
	 * it determines whether the work is a book or an article and redirects to the appropriate editing page.
	 * 
	 * <p>
	 * The following steps are performed:
	 * <ul>
	 *   <li>Check if the work ID is provided.</li>
	 *   <li>Look for the work in the database using the provided ID.</li>
	 *   <li>If the work is found and is of type "BOOK", redirect to the book editing page.</li>
	 *   <li>If the work is found and is of type "ARTICLE", redirect to the article editing page.</li>
	 *   <li>If the work is not found, print a message and redirect to the list of works.</li>
	 *   <li>If no ID is provided, print a message and redirect to the list of works.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param id The ID of the work to be edited. This ID is used to fetch the work from the database.
	 * @param model The model to which the work data is added (although not used directly in this method).
	 * @return A redirection URL:
	 *         <ul>
	 *           <li>If the work exists and is a book, redirect to the book editing page.</li>
	 *           <li>If the work exists and is an article, redirect to the article editing page.</li>
	 *           <li>If the work doesn't exist or the ID is not provided, redirect to the list of works.</li>
	 *         </ul>
	 */
	@GetMapping("/workList/edit{id}")
	public String editarObra(@PathVariable("id") Long id, Model model) {
		String retorno="";
		if (id!=null) {
			
			Optional<Obra> obra = obraRepo.findById(id);	
		
			if (obra!=null && obra.isPresent()) {
				if (obra.get().getTipo().equalsIgnoreCase("BOOK")) {
					retorno="redirect:/workEdit/book?id="+id;
				} else {
					retorno="redirect:/workEdit/article?id="+id;
				}
			} else {
				System.out.println("No se encontró la obra con el isbn: "+id);
				retorno="redirect:/workList";
			}
		} else {
			System.out.println("No se ha recibido id");
			retorno="redirect:/workList";
		}
		return retorno;
	}

	/**
	 * This method handles the editing functionality for books in the system.
	 * It retrieves a book by its ID and populates the model with necessary data to edit it.
	 * The model is populated with the book information, tags, and the type of the work.
	 * 
	 * <p>
	 * The following steps are performed:
	 * <ul>
	 *   <li>Check if the book ID is provided.</li>
	 *   <li>If the book ID is valid, retrieve the book from the database using the ID.</li>
	 *   <li>If the book is found, collect a list of tags (themes) and add them to the model.</li>
	 *   <li>Add the book's data to the model for display in the edit view.</li>
	 *   <li>If the book is not found or no ID is provided, redirect to the list of works.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param id The ID of the book to be edited. This ID is used to fetch the book from the database.
	 * @param model The model to which the book and other necessary data are added for the view.
	 * @return A string indicating the view to be rendered:
	 *         <ul>
	 *           <li>If the book is found, render the work edit page with the book's data.</li>
	 *           <li>If the book is not found or the ID is invalid, redirect to the list of works.</li>
	 *         </ul>
	 */
	@GetMapping("/workEdit/book")
	public String editarLibro(@RequestParam Long id, Model model) {
		String retorno="";
		if (id != null) {
			Optional<Obra> obra = obraRepo.findById(id);
			if (obra != null && obra.isPresent()) {
					
				List<String> tags = obraService.findAllDifferentTemas();
				System.out.println("Tags recogidos: "+tags.toString());

				model.addAttribute("tagsJson", tags);
				model.addAttribute("obraEdit", obra.get());
				model.addAttribute("type", obra.get().getTipo().toUpperCase());
				retorno = "workEdit";
			} else {
				System.out.println("No se ha encontrado la obra para editar");
				retorno = "redirect:/workList";
			}
		} else {
			System.out.println("No se ha recibido id para editar");
			retorno = "redirect:/workList";
		}
		
		return retorno;
	}
	
	/**
	 * This method handles the submission of the book editing form.
	 * It receives the edited book data, validates it, and updates the book record in the database.
	 * 
	 * <p>
	 * The following steps are performed:
	 * <ul>
	 *   <li>Validate the input data for the edited book.</li>
	 *   <li>If there are validation errors, redirect to the book edit page with a failure message.</li>
	 *   <li>If the book's ISBN exists in the database, the existing record is deleted and the updated book is saved.</li>
	 *   <li>If the book's ISBN does not exist, the updated book is saved as a new entry.</li>
	 *   <li>In either case, the book's tags (themes) are parsed and added to the book record.</li>
	 *   <li>After the update, redirect to the work list page.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param editedWork The edited book object, bound to the form data.
	 * @param tags A string of comma-separated tags (themes) associated with the book.
	 * @param br The binding result that holds any validation errors.
	 * @return A string indicating where to redirect the user:
	 *         <ul>
	 *           <li>If there are validation errors or the book already exists, redirect to the book edit page with a failure message.</li>
	 *           <li>If the book is successfully updated, redirect to the work list page.</li>
	 *         </ul>
	 */
	@PostMapping("/workEdit/bookSubmit")
	public String editarLibroSubmit(@Valid @ModelAttribute("workEdit") Obra editedWork,@RequestParam("tags") String tags, BindingResult br) {
		String retorno="";
		
		System.out.println("Insertando "+editedWork.toString());
		Optional<Obra> test = obraRepo.findByIsbn(editedWork.getIsbn());
		if (br.hasErrors()){
			// This could use to give more information to the user
			retorno = "redirect:/workEdit/book?failure=Problems to update the book or it exist";
		} else  if (test.isPresent()){
			
			// converts the string in a List of strings separated by comas and insert in the object
			List<String> tagsList = Arrays.asList(tags.split(","));
			editedWork.setTemas(tagsList);
			
			obraRepo.deleteByIsbn(editedWork.getIsbn());
			editedWork.setTipo("BOOK");
			obraRepo.save(editedWork);
			retorno="redirect:/workList";
			
		} else {
			
			editedWork.setTipo("BOOK");
			obraRepo.save(editedWork);
			retorno="redirect:/workList";
		}
		
		return retorno;
	}
	
	/**
	 * This method handles the editing of an article in the system.
	 * It retrieves the article based on its ID and prepares it for editing.
	 * The method also fetches a list of available tags (themes) for the article.
	 * 
	 * <p>
	 * The following steps are performed:
	 * <ul>
	 *   <li>Check if a valid article ID is provided.</li>
	 *   <li>If the article exists in the database, fetch it and load its data into the model.</li>
	 *   <li>Retrieve a list of tags that can be associated with the article.</li>
	 *   <li>Prepare the model for the editing view, including the article and the available tags.</li>
	 *   <li>If the article ID is invalid or not found, redirect to the work list page.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param id The ID of the article to be edited.
	 * @param model The model to which attributes are added for the view.
	 * @return A string indicating the next view:
	 *         <ul>
	 *           <li>If the article exists, return the view name for editing the article.</li>
	 *           <li>If the article is not found, redirect to the work list page.</li>
	 *         </ul>
	 */
	@GetMapping("/workEdit/article{id}")
	public String editarArticulo(@RequestParam Long id, Model model) {
		String retorno="";
		if (id != null) {
			Optional<Obra> obra = obraRepo.findById(id);
			if (obra != null && obra.isPresent()) {
				
				List<String> tags = obraService.findAllDifferentTemas();
				System.out.println("Tags recogidos: "+tags.toString());

				model.addAttribute("tagsJson", tags);
				model.addAttribute("obraEdit", obra.get());
				model.addAttribute("type", obra.get().getTipo().toUpperCase());
				retorno = "workEdit";
			}
		} else {
			System.out.println("no se ha recibido id para editar");
			retorno = "redirect:/workList";
		}
		
		return retorno;
	}
	
	/**
	 * This method handles the submission of the article edit form. It validates the form data, checks if the article already exists
	 * in the system, and then either updates the article or creates a new one.
	 * 
	 * <p>
	 * The following steps are performed:
	 * <ul>
	 *   <li>Validate the form data and check for any errors.</li>
	 *   <li>If there are validation errors, redirect to the article edit page with an error message.</li>
	 *   <li>If the article already exists in the system (checked by ISBN), delete the old version and save the edited article.</li>
	 *   <li>If the article doesn't exist, simply save the new article with the updated information.</li>
	 *   <li>Redirect to the work list page after the article has been successfully saved or updated.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param editedWork The edited article object containing the updated information.
	 * @param tags A string containing the tags (themes) associated with the article, separated by commas.
	 * @param br The binding result used to validate the form data.
	 * @return A string indicating the next view:
	 *         <ul>
	 *           <li>If there are errors in the form, redirect to the article edit page with an error message.</li>
	 *           <li>If the article was updated or created successfully, redirect to the work list page.</li>
	 *         </ul>
	 */
	@PostMapping("/workEdit/articleSubmit")
	public String editarArticuloSubmit(@Valid @ModelAttribute("workEdit") Obra editedWork,@RequestParam("tags") String tags, BindingResult br) {
		String retorno="";
		
		System.out.println("Insertando "+editedWork.toString());
		Optional<Obra> test = obraRepo.findByIsbn(editedWork.getIsbn());
		if (br.hasErrors()){
			// This could use to give more information to the user
			retorno = "redirect:/workEdit/article?failure=Problems to update the book or it exist";
		} else  if (test.isPresent()){
			
			// converts the string in a List of strings separated by comas and insert in the object
			List<String> tagsList = Arrays.asList(tags.split(","));
			editedWork.setTemas(tagsList);
			
			obraRepo.deleteByIsbn(editedWork.getIsbn());
			editedWork.setTipo("ARTICLE");
			obraRepo.save(editedWork);
			retorno="redirect:/workList";
			
		} else {
			
			editedWork.setTipo("ARTICLE");
			obraRepo.save(editedWork);
			retorno="redirect:/workList";
		}
		
		return retorno;
	}
	
	/**
	 * This method retrieves and displays a list of all comments in the system.
	 * 
	 * <p>
	 * It checks if there are any comments in the repository. If there are comments, they are added to the model 
	 * and the "commentList" view is returned to display the comments. If no comments are found, the user is redirected 
	 * back to the index page.
	 * </p>
	 *
	 * @param model The Spring model object that holds attributes to be passed to the view.
	 * @return A string indicating the next view:
	 *         <ul>
	 *           <li>If comments are found, the "commentList" view is returned to display the comments.</li>
	 *           <li>If no comments are found, the user is redirected to the index page ("/").</li>
	 *         </ul>
	 */
	@GetMapping("/commentList")
	public String listarComentarios(Model model) {
		String retorno="";
		List<Comentario> comments = commentRepo.findAll();
		if (comments != null && !comments.isEmpty()) {
			model.addAttribute("comments", comments);
			retorno="commentList";
		} else {
			System.out.println("No se encontraron comentarios volviendo a index");
			retorno="/";
		}
		return retorno;
	}
	
	/**
	 * This method handles the banning of a comment by its ID.
	 * 
	 * <p>
	 * It bans the comment using the `commentService.banComment` method. Depending on the value of the 
	 * `origin` parameter, the user is redirected either back to the work show page ("/workShow?id={obraId}") 
	 * or to the comment list page ("/commentList").
	 * </p>
	 *
	 * @param commentId The ID of the comment to be banned.
	 * @param obraId The ID of the work related to the comment (used for redirection if the origin is "workShow").
	 * @param origin A string indicating the origin of the request. If the value is "workShow", the user is redirected to the work show page. 
	 *               Otherwise, the user is redirected to the comment list page.
	 * @return A string representing the view to be returned:
	 *         <ul>
	 *           <li>If the origin is "workShow", the user is redirected to the work show page with the given obraId.</li>
	 *           <li>Otherwise, the user is redirected to the comment list page.</li>
	 *         </ul>
	 */
	@GetMapping("/banComment")
	public String bannearComentario(@RequestParam String commentId, @RequestParam Long obraId, @RequestParam String origin) {
		String retorno="";
		commentService.banComment(commentId);
		if (origin.equals("workShow")) {
			retorno = "redirect:/workShow?id="+obraId; 
		} else {
			retorno = "redirect:/commentList";
		}
		return retorno;
	}
	
	/**
	 * This method handles the unbanning of a comment by its ID.
	 * 
	 * <p>
	 * It calls the `commentService.unbanComment` method to remove the ban from the specified comment.
	 * After unbanning the comment, the user is redirected to the comment list page.
	 * </p>
	 *
	 * @param id The ID of the comment to be unbanned.
	 * @return A string representing the view to be returned, which is a redirect to the comment list page.
	 */
	@GetMapping("/unbanComment")
	public String desbannearComentario(@RequestParam String id) {
		commentService.unbanComment(id);
		return "redirect:/commentList";
	}
	
	
	
}
