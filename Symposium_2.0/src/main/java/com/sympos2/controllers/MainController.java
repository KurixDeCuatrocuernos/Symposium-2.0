package com.sympos2.controllers;

import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.sympos2.models.Usuario;
import com.sympos2.repositories.UserRepository;
import com.sympos2.securities.SessionUtils;
import com.sympos2.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class MainController {
	
	@Autowired
	private UserRepository UserRepo;
	
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
	@GetMapping("/admin-zone-users-list")
	public String listado(Model model) {
		String retorno = "";
		Usuario usuario = SessionUtils.getCurrentUsuario();
		if (usuario != null && "ADMIN".equals(usuario.getRole())) {
			retorno = "/admin-zone-users-list";
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
			retorno="redirect:/admin-zone-users-list";
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
		return "redirect:/admin-zone-users-list";
		
	}
}
