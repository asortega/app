package unicauca.front.end.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.github.javafaker.Faker;
import com.google.gson.Gson;

import controladores.EmbargosController;
import enumeraciones.Ciudad;
import enumeraciones.Departamento;
import enumeraciones.TipoAutoridad;
import enumeraciones.TipoIdentificacion;
import modelo.Autoridad;
import modelo.Persona;
import modelo.Usuario;
import unicauca.front.end.service.Consulta;

@Controller
@RequestMapping("/admin/autoridad")
public class AutoridadController {

	private Faker faker;
	private String[] lst = { "GESTOR", "SECRETARIO" };
	private Authentication authentication;
	private Autoridad autoridad = new Autoridad();
	private Usuario usuario = new Usuario();
	
	private String boton;
	
	@GetMapping("/main")
	public String main(Model model) {
		Usuario usuario = new Usuario();
		model.addAttribute("titulo", "App");
		model.addAttribute("form", "Formulario");
		model.addAttribute("usuario", usuario);
		model.addAttribute("lst", lst);
		model.addAttribute("boton", "all");
		return "autoridad/admin/main";
	}
	
	@RequestMapping(value = "/form", method = RequestMethod.POST, params = "action=crear")
	public String admin(@ModelAttribute(name = "usuario") Usuario usuario, Model model, RedirectAttributes flash) {
		authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();
		if (checkUsuario(usuario)) {
			if (usuario.getPassword().equals(usuario.getConfirmPassword())) {
				if (BackEndController.obtenerUsuario(usuario.getUsername()) == null) {
					// Buscar en fabric la autoridad por el ownedBy
					Usuario usuarioLogin = BackEndController.obtenerUsuario(username);
					Autoridad autoridad = BackEndController.obtenerAutoridad(usuarioLogin.getOwnedBy());
					usuario.getRoles().add(autoridad.getTipoAutoridad().toString());
					EmbargosController.guardarUsuario(usuario);
					flash.addFlashAttribute("success", "Usuario creado con exito");
				} else {
					flash.addFlashAttribute("error", "No se puede Crear,Nombre de Usuario ya existe");
				}
			} else {
				flash.addFlashAttribute("error", "No se puede Crear,Contraseñas no coinciden");
			}
		} else {
			flash.addFlashAttribute("error", "No se puede Crear ,Por favor llenar el formulario");
		}

		flash.addFlashAttribute("boton", "all");
		flash.addFlashAttribute("usuario", usuario);
		return "redirect:/admin/autoridad/crear";
	}

	@GetMapping("/crear")
	public String system(Model model) {
		model.addAttribute("titulo", "App");
		model.addAttribute("form", "Formulario");
		model.addAttribute("lst", lst);
		return "autoridad/admin/main";
	}

	@RequestMapping(value = "/form", method = RequestMethod.POST, params = "action=consultar")
	public String consultar(@ModelAttribute(name = "usuario") Usuario usuario, Model model,RedirectAttributes flash) throws JSONException {
		flash.addFlashAttribute("usuario", usuario);
		return "redirect:/admin/autoridad/consulta";
	}
	
	@GetMapping("/consulta")
	public String loadConsulta(@ModelAttribute(name = "usuario") Usuario usuario,Model model, RedirectAttributes flash) throws JSONException {
		Consulta selector = new Consulta();
		if (!consulta(usuario).isEmpty()) {
			selector.setSelector(consulta(usuario));
			Gson gson = new Gson();
			String consulta = gson.toJson(selector);
			System.out.println("Consulta: " + consulta);
			String mensaje="{\"key\":1,\"Record\":{\"identificacion\":123,\"tipoIdentificacion\":\"NATURAL\",\"nombres\":\"santiago\",\"apellidos\":\"ortega\",\"username\":\"as\",\"roles\":[\"GESTOR\",\"JUDICIAL\"]}}";
			// String mensaje = EmbargosController.consultaGeneral(consulta);
			System.out.println("Mensaje: " + mensaje);
			ArrayList<Usuario> usuarios = new ArrayList<Usuario>();
			mensaje = "[" + mensaje + "]";
			JSONArray myjson = new JSONArray(mensaje);
			for (int i = 0; i < myjson.length(); i++) {
				JSONObject jsonRecord = myjson.getJSONObject(i).getJSONObject("Record");
				usuarios.add(jsontoObject(jsonRecord));
			}
			for (Usuario usuario2 : usuarios) {
				System.out.println("Roles: "+usuario2.getRoles());
			}
			model.addAttribute("titulo", "Consulta");
			model.addAttribute("form", "Consultas");
			model.addAttribute("usuarios", usuarios);
			boton = "actualizar";
			model.addAttribute("boton", boton);
			return "autoridad/admin/consulta";
		} else {
			flash.addFlashAttribute("warning", "No se puede Consultar, Por favor ingresar el campo a consultar");
			return "redirect:/admin/autoridad/main";
		}
		
	}

	@RequestMapping(value = "/form", method = RequestMethod.POST, params = "action=actualizar")
	public String actualizar(@ModelAttribute(name = "usuario") Usuario usuario, Model model) {

		/*
		 * System.out.println("id Usuario:" + usuario.getIdentificacion());
		 * System.out.println("tipo Usuario:" + usuario.getTipoIdentificacion());
		 * System.out.println("Nombres:" + usuario.getIdentificacion());
		 * System.out.println("username:" + usuario.getIdentificacion());
		 * System.out.println("Habilitado:" + usuario.isHabilitado());
		 * System.out.println("Roles:" + usuario.getRoles());
		 */
		Usuario usuarionew= BackEndController.obtenerUsuario(usuario.getUsername());
		model.addAttribute("titulo", "Consulta");
		model.addAttribute("form", "Consultas");
		model.addAttribute("usuario", usuarionew);
		model.addAttribute("lst", lst);
		model.addAttribute("boton", "onactualizar");
		return "autoridad/admin/main";

	}

	@RequestMapping(value = "/form", method = RequestMethod.POST, params = "action=onactualizar")
	public String onactualizar(@ModelAttribute(name = "usuario") Usuario usuario, Model model,
			RedirectAttributes flash) {
		flash.addFlashAttribute("success", "Usuario actualizado con exito");
		System.out.println("id Usuario:" + usuario.getIdentificacion());
		System.out.println("tipo Usuario:" + usuario.getTipoIdentificacion());
		System.out.println("Nombres:" + usuario.getIdentificacion());
		System.out.println("username:" + usuario.getIdentificacion());
		System.out.println("roles usuario:" + usuario.getRoles());
		System.out.println("estado:" + usuario.isHabilitado());
		flash.addFlashAttribute("usuario", usuario);
		flash.addFlashAttribute("boton", "actualizar");
		return "redirect:/admin/autoridad/consulta";

	}


	@RequestMapping(value = "/form", method = RequestMethod.POST, params = "action=habilitar")
	public String habilitar(@ModelAttribute(name = "usuario") Usuario usuario, Model model, RedirectAttributes flash) {
		System.out.println("id Usuario:" + usuario.getIdentificacion());
		System.out.println("Habilitado:" + usuario.isHabilitado());
		ArrayList<Usuario> usuarios = new ArrayList<Usuario>();
		usuario.setHabilitado(true);
		for (int i = 0; i < 3; i++) {
			usuarios.add(usuario);
		}
		model.addAttribute("titulo", "Consulta");
		model.addAttribute("form", "Consultas");
		model.addAttribute("usuarios", usuarios);
		model.addAttribute("boton", "actualizar");
		return "autoridad/admin/consulta";
	}

	@RequestMapping(value = "/form", method = RequestMethod.POST, params = "action=deshabilitar")
	public String deshabilitar(@ModelAttribute(name = "usuario") Usuario usuario, Model model,
			RedirectAttributes flash) {
		System.out.println("id Usuario:" + usuario.getIdentificacion());
		System.out.println("Habilitado:" + usuario.isHabilitado());
		usuario.setHabilitado(false);
		ArrayList<Usuario> usuarios = new ArrayList<Usuario>();
		for (int i = 0; i < 3; i++) {
			usuarios.add(usuario);
		}
		model.addAttribute("titulo", "Consulta");
		model.addAttribute("form", "Consultas");
		model.addAttribute("usuarios", usuarios);
		model.addAttribute("boton", "actualizar");
		return "autoridad/admin/consulta";
	}

	public HashMap<String, String> consulta(Usuario usuario) {
		HashMap<String, String> campos = new HashMap<String, String>();

		if (!usuario.getIdentificacion().isEmpty()) {
			campos.put("identificacion", usuario.getIdentificacion());
		} else {
			if (usuario.getTipoIdentificacion() != null) {
				campos.put("tipoIdentificacion", usuario.getTipoIdentificacion().toString());
			} else {
				if (!usuario.getNombres().isEmpty()) {
					campos.put("nombres", usuario.getNombres());
				} else {
					if (!usuario.getApellidos().isEmpty()) {
						campos.put("apellidos", usuario.getApellidos());
					} else {
						if (!usuario.getUsername().isEmpty()) {
							campos.put("username", usuario.getUsername());
						}
					}
				}
			}
		}
		return campos;
	}

	public Usuario jsontoObject(JSONObject jsonRecord) throws JSONException {
		Usuario usuario = new Usuario();

		if (jsonRecord.has("identificacion")) {
			usuario.setIdentificacion(jsonRecord.getString("identificacion"));
		}
		
		if (jsonRecord.has("tipoIdentificacion")) {
			usuario.setTipoIdentificacion(TipoIdentificacion.valueOf(jsonRecord.getString("tipoIdentificacion")));
		}
		if (jsonRecord.has("nombres")) {
			usuario.setNombres(jsonRecord.getString("nombres"));
		}
		if (jsonRecord.has("apellidos")) {
			usuario.setApellidos(jsonRecord.getString("apellidos"));
		}
		if (jsonRecord.has("username")) {
			usuario.setUsername(jsonRecord.getString("username"));
		}
		ArrayList<String> roles = new ArrayList<String>();
		if (jsonRecord.has("roles")) {
			JSONArray jsonRoles = jsonRecord.getJSONArray("roles");

			for (int k = 0; k < jsonRoles.length(); k++) {
				roles.add(jsonRoles.getString(k));
			}
			usuario.setRoles(roles);
		}		
		return usuario;
	}

	/*
	 * public Usuario createUsuario() { faker = new Faker(); String identificacion =
	 * "persona" + ThreadLocalRandom.current().nextInt(1, 99999 + 1);
	 * TipoIdentificacion tipoIdentificacion =
	 * TipoIdentificacion.values()[ThreadLocalRandom.current().nextInt(0,
	 * TipoIdentificacion.values().length)]; String nombres =
	 * faker.name().firstName(); String username = faker.name().username(); String
	 * apellidos = faker.name().lastName();
	 * 
	 * ArrayList<String> roles = new ArrayList<>(); roles.add("GESTOR");
	 * roles.add("JUDICIAL"); boolean habilitado = true; Usuario usuario = new
	 * Usuario(identificacion, nombres, apellidos, tipoIdentificacion, username,
	 * "123", "123", roles, habilitado); // persona.setEstado("INACTIVO"); return
	 * usuario; }
	 */

	private boolean checkUsuario(Usuario usuario) {
		return !usuario.getIdentificacion().isEmpty() && usuario.getTipoIdentificacion() != null
				&& !usuario.getNombres().isEmpty() && !usuario.getApellidos().isEmpty()
				&& !usuario.getUsername().isEmpty() && !usuario.getRoles().isEmpty() && !usuario.getPassword().isEmpty()
				&& !usuario.getConfirmPassword().isEmpty();
	}
}