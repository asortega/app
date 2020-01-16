package unicauca.front.end.auth;

import java.security.Principal;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import controladores.EmbargosController;
import modelo.Persona;
import modelo.Usuario;

@Controller
public class LoginController {


	@GetMapping("/login")
	public String login(@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "logout", required = false) String logout, Model model, Principal principal,
			RedirectAttributes flash) {

		if (principal != null) {
			flash.addFlashAttribute("info", "Ya iniciado sesión anteriormente");
		}

		if (error != null) {
			model.addAttribute("error",
					"Error en el login: Nombre de usuario o contraseña incorrecta, por favor vuelva a intentarlo!");
		}
		model.addAttribute("titulo", "Login");
		
		return "login";
	}
	
	@GetMapping("/crear")
	public String usuario(Model model) {

		Persona persona = new Persona();
		Usuario usuario = new Usuario();
		model.addAttribute("titulo", "Login");
		model.addAttribute("form", "Formulario");
		model.addAttribute("persona", persona);
		model.addAttribute("usuario", usuario);
		return "crear";
	}

	@PostMapping("/crear")
	public String crear(@ModelAttribute(name = "persona") Persona persona,
			@ModelAttribute(name = "usuario") Usuario usuario, Model model, RedirectAttributes flash) {

		flash.addFlashAttribute("success", "Usuario creado con exito, por favor iniciar sesion");
		ArrayList<String> roles = new ArrayList<>();
		roles.add("PERSONA");
		usuario.setRoles(roles);
		//EmbargosController.guardarUsuario(usuario);

		return "redirect:login";
	}

}
