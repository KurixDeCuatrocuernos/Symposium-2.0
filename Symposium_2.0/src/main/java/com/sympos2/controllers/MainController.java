package com.sympos2.controllers;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sympos2.models.Obra;
import com.sympos2.models.Usuario;
import com.sympos2.repositories.ObraRepository;
import com.sympos2.repositories.UserRepository;
import com.sympos2.services.ObraService;
import com.sympos2.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class MainController {
	
	@Autowired
	private UserRepository UserRepo;
	
	@Autowired
	private ObraRepository obraRepo;
	
	@Autowired
	private ObraService obraService;
	
	@Autowired
	private UserService service;
	
	@Autowired
	private AuthenticationManager authManager;
	
	@Autowired
	private BCryptPasswordEncoder encoder;
	
	@Autowired
	private HttpServletRequest request;  // Spring inyecta el HttpServletRequest automáticamente

	
//	@Autowired
//	private StorageService storage;
	
	@GetMapping("/")
	public String index(Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		System.out.println("Estoy en la página principal");
		if (auth != null && auth.isAuthenticated()) {
			model.addAttribute("username", auth.getName());
		} else {
			model.addAttribute("username", "invitado");
		}
		return "/index";
	}
	
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
	
	@GetMapping("/loginError")
	public String loginError(Model modelo) {
		// cosas
		return "/loginError";
	}
	
	// mappings for user
	@GetMapping("/usersList")
	public String listado(Model model) {
		String retorno = "";
		Usuario usuario = getCurrentUsuario();
		if (usuario != null && "ADMIN".equals(usuario.getRole())) {
			retorno = "/usersList";
			model.addAttribute("listaUsuarios", UserRepo.findAll());
		} else {
			System.out.println("El usuario no es administrador");
			retorno = "/";
		}
		return retorno;
	}
	
	@GetMapping("form")
	public String registro(Model model) {
		model.addAttribute("usuarioEdit", null);
		return "/form";
	}
	
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
			UserRepo.save(nuevoUsuario);
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
	
	@GetMapping("/edit{id}")
	public String editarUsuarioForm(@PathVariable("id") String id, Model model) {
		String retorno="";
		Optional<Usuario> user = UserRepo.findById(id);
		System.out.println("recogiendo el usuario"+ user.get().toString());
		if (user.isPresent()) {
			model.addAttribute("usuarioEdit", user.get());
			retorno = "form";
		} else {
			retorno = "redirect:/form";
		}
		return retorno;
	}
	
	@PostMapping("/edit/submit")
	public String editarUsuarioSubmit(@Valid @ModelAttribute("userForm") Usuario editarUsuario, BindingResult br) {
		String retorno = "";
		System.out.println("Modificando: "+editarUsuario.toString());
		if (br.hasErrors()){
			retorno = "form";
		} else {
			service.edit(editarUsuario);
			retorno="redirect:/usersList";
		}
		
		return retorno;
	}
	
	@GetMapping("/userlist/delete")
	public String deleteUser(@RequestParam String id, Model model) {
		Optional<Usuario> user = UserRepo.findById(id);
		if (user.isPresent()) {
			System.out.println("Se ha borrado al usuario: ");
			System.out.println(user.get().getId().toString());
			UserRepo.deleteById(user.get().getId());
		} 
		return "redirect:/usersList";
		
	}
	
	public Usuario getCurrentUsuario() {
        Usuario retorno=null;
        
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Usuario) {
            retorno = (Usuario) authentication.getPrincipal();
        }
        return retorno;
    }
	
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
		
	@GetMapping("/workList/delete{id}")
	public String borrarObra(Long id) {
		Optional<Obra> obra = obraRepo.findById(id);
		if (obra.isPresent()) {
			System.out.println("Se ha borrado la obra: ");
			System.out.println(obra.get().getIsbn()+obra.get().getTitulo());
			obraRepo.deleteByIsbn(obra.get().getIsbn());
		} 
		return "redirect:/workList";
	}
	
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
	
}
