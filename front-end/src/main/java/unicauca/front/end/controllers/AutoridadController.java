package unicauca.front.end.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.github.javafaker.Faker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

	private String[] lst = { "GESTOR", "SECRETARIO" };
	private Authentication authentication;

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

	@RequestMapping(value = "/form/{consulta}", method = RequestMethod.POST, params = "action=crear")
	public String admin(@ModelAttribute(name = "usuario") Usuario usuario, Model model, RedirectAttributes flash)
			throws JSONException {
		authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();
		if (checkUsuario(usuario)) {
			if (BackEndController.obtenerUsuarioByid(usuario.getIdentificacion()) == null) {
				if (BackEndController.obtenerUsuario(usuario.getUsername()) == null) {
					if (usuario.getPassword().equals(usuario.getConfirmPassword())) {
						Usuario usuarioLogin = BackEndController.obtenerUsuario(username);
						Autoridad autoridad = BackEndController.obtenerAutoridad(usuarioLogin.getOwnedBy());
						usuario.setOwnedBy(usuarioLogin.getIdentificacion());
						usuario.setIdAutoridad(usuarioLogin.getIdAutoridad());
						usuario.getRoles().add(autoridad.getTipoAutoridad().toString());
						EmbargosController.guardarUsuario(usuario);
						flash.addFlashAttribute("success", "Usuario creado con exito");
					} else {
						flash.addFlashAttribute("error", "No se puede Crear,Contraseñas no coinciden");
					}
				} else {
					flash.addFlashAttribute("error", "No se puede Crear,Nombre de Usuario ya existe");
					usuario.setUsername(null);
				}
			} else {
				flash.addFlashAttribute("error", "No se puede Crear,Identificacion ya existe");
				usuario.setIdentificacion(null);
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

	@RequestMapping(value = "/form/{consulta}", method = RequestMethod.POST, params = "action=consultar")
	public String consultar(@ModelAttribute(name = "usuario") Usuario usuario, Model model, RedirectAttributes flash)
			throws JSONException {
		
		if (emptyField(usuario)) {
			Gson gson = new Gson();
			String consulta = gson.toJson(consulta(usuario));
			ArrayList<Usuario> usuarios = jsontoArray(consulta);
			if (!usuarios.isEmpty()) {
				model.addAttribute("titulo", "Consulta");
				model.addAttribute("form", "Consultas");
				model.addAttribute("usuarios", usuarios);
				model.addAttribute("consulta", consulta);
				boton = "actualizar";
				model.addAttribute("boton", boton);
				return "autoridad/admin/consulta";
			} else {
				flash.addFlashAttribute("warning", "No se encontraron resultados");
				return "redirect:/admin/autoridad/main";
			}
		} else {
			flash.addFlashAttribute("warning", "No se puede Consultar, Por favor ingresar el campo a consultar");
			return "redirect:/admin/autoridad/main";
		}
	}

	public String buildSelector(Usuario usuario) {

		authentication = SecurityContextHolder.getContext().getAuthentication();
		String usernamenew = authentication.getName();
		Usuario usuarioLogin = BackEndController.obtenerUsuario(usernamenew);
		String selectors = "";

		if (!usuario.getIdentificacion().isEmpty()) {
			selectors = "\"identificacion\":\"" + usuario.getIdentificacion() + "\"";
		}
		if (usuario.getTipoIdentificacion() != null) {
			selectors += "," + "\"tipoIdentificacion\":\"" + usuario.getTipoIdentificacion().toString() + "\"";
		}
		if (!usuario.getNombres().isEmpty()) {
			selectors += "," + "\"nombres\":\"" + usuario.getNombres() + "\"";
		}
		if (!usuario.getApellidos().isEmpty()) {
			selectors += "," + "\"apellidos\":\"" + usuario.getApellidos() + "\"";
		}
		if (!usuario.getUsername().isEmpty()) {
			selectors += "," + "\"username\":\"" + usuario.getUsername() + "\"";
		}
		if (!usuario.getRoles().isEmpty()) {
			selectors += "," + "\"roles\":{" + "\"$elemMatch\":{" + "\"$eq\":\"" + usuario.getRoles().get(0) + "\"}}";
		}
		if (!selectors.isEmpty()) {
			selectors += "," + "\"ownedBy\":\"" + usuarioLogin.getIdentificacion() + "\"";
		}
		return selectors;
	}

	@RequestMapping(value = "/form/{consulta}", method = RequestMethod.POST, params = "action=actualizar")
	public String actualizar(@ModelAttribute(name = "usuario") Usuario usuario, Model model,
			@PathVariable(value = "consulta") String consulta) {
		Usuario usuarionew = BackEndController.obtenerUsuario(usuario.getUsername());
		model.addAttribute("titulo", "Consulta");
		model.addAttribute("form", "Consultas");
		model.addAttribute("usuario", usuarionew);
		model.addAttribute("lst", lst);
		model.addAttribute("boton", "onactualizar");
		model.addAttribute("consulta", consulta);
		return "autoridad/admin/main";
	}

	@RequestMapping(value = "/form/{consulta}", method = RequestMethod.POST, params = "action=onactualizar")
	public String onactualizar(@ModelAttribute(name = "usuario") Usuario usuario, Model model, RedirectAttributes flash,
			@PathVariable(value = "consulta") String consulta) {
		boolean band = false;
		if (checkUsuario(usuario)) {
			if (usuario.getPassword().equals(usuario.getConfirmPassword())) {
				Usuario userfind = BackEndController.obtenerUsuario(usuario.getUsername());
				updateUser(userfind, usuario);
				band = true;
			} else {
				flash.addFlashAttribute("error",
						"No se puede Actualizar Autoridad,Contraseñas de Usuario no coinciden");
				band = false;
			}
		} else {
			flash.addFlashAttribute("error", "No se puede Actualizar Usuario,Por favor llenar el formulario");
			band = false;
		}
		if (band == true) {
			flash.addFlashAttribute("success", "Usuario actualizado con exito");
			flash.addFlashAttribute("boton", "actualizar");
			flash.addFlashAttribute("consulta", consulta);
			return "redirect:/admin/autoridad/consulta";
		} else {
			flash.addFlashAttribute("usuario", usuario);
			return "redirect:/admin/autoridad/actualizar";
		}
	}

	@GetMapping("/consulta")
	public String loadConsulta(@ModelAttribute(name = "consulta") String consulta, Model model) throws JSONException {

		ArrayList<Usuario> usuarios = jsontoArray(consulta);
		model.addAttribute("titulo", "Consulta");
		model.addAttribute("form", "Consultas");
		model.addAttribute("usuarios", usuarios);
		model.addAttribute("consulta", consulta);
		return "autoridad/admin/consulta";
	}

	@GetMapping("/actualizar")
	public String loadActualizar(Model model) {
		model.addAttribute("lst", lst);
		model.addAttribute("titulo", "Actualizar");
		model.addAttribute("form", "Formulario");
		model.addAttribute("boton", "onactualizar");
		return "autoridad/admin/main";
	}

	public void updateUser(Usuario userfind, Usuario usuario) {
		userfind.setTipoIdentificacion(usuario.getTipoIdentificacion());
		userfind.setNombres(usuario.getNombres());
		userfind.setApellidos(usuario.getApellidos());
		userfind.setPassword(usuario.getPassword());
		userfind.setConfirmPassword(usuario.getPassword());
		userfind.getRoles().set(0, usuario.getRoles().get(0));
		userfind.setHabilitado(usuario.isHabilitado());
		EmbargosController.editarUsuario(userfind);
		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Consulta consulta(Usuario usuario) {
		
		authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();
		Usuario usuarioLogin = BackEndController.obtenerUsuario(username);
		Consulta consulta = new Consulta();
		
		if (!usuario.getIdentificacion().isEmpty()) {
			consulta.searchNormal("identificacion", usuario.getIdentificacion());
		}
		if (usuario.getTipoIdentificacion() != null) {
			consulta.searchNormal("tipoIdentificacion", usuario.getTipoIdentificacion().toString());
		}
		if (!usuario.getNombres().isEmpty()) {
			consulta.searchNormal("nombres", usuario.getNombres());
		}
		if (!usuario.getApellidos().isEmpty()) {
			consulta.searchNormal("apellidos", usuario.getApellidos());
		}
		if (!usuario.getUsername().isEmpty()) {
			consulta.searchNormal("username", usuario.getUsername());
		}
		if (!usuario.getRoles().isEmpty()) {
			consulta.searchRol(usuario.getRoles().get(0));
		}
		consulta.searchNormal("ownedBy", usuarioLogin.getIdentificacion());
	
		return consulta;
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
		if (jsonRecord.has("ownedBy")) {
			usuario.setOwnedBy(jsonRecord.getString("ownedBy"));
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

	public ArrayList<Usuario> jsontoArray(String consulta) throws JSONException {
		String consultanew = consulta;
		ArrayList<Usuario> usuarios = new ArrayList<Usuario>();
		String mensaje = EmbargosController.consultaGeneral(consultanew);
		mensaje = "[" + mensaje + "]";
		JSONArray myjson = new JSONArray(mensaje);
		for (int i = 0; i < myjson.length(); i++) {
			JSONObject jsonRecord = myjson.getJSONObject(i).getJSONObject("Record");
			usuarios.add(jsontoObject(jsonRecord));
		}
		return usuarios;
	}

	public boolean emptyField(Usuario usuario) {
		return !usuario.getIdentificacion().isEmpty() || usuario.getTipoIdentificacion() != null
				|| !usuario.getNombres().isEmpty() || !usuario.getApellidos().isEmpty()
				|| !usuario.getUsername().isEmpty() || !usuario.getRoles().isEmpty() || !usuario.getPassword().isEmpty()
				|| !usuario.getConfirmPassword().isEmpty();
	}

	private boolean checkUsuario(Usuario usuario) {
		return !usuario.getIdentificacion().isEmpty() && usuario.getTipoIdentificacion() != null
				&& !usuario.getNombres().isEmpty() && !usuario.getApellidos().isEmpty()
				&& !usuario.getUsername().isEmpty() && !usuario.getRoles().isEmpty() && !usuario.getPassword().isEmpty()
				&& !usuario.getConfirmPassword().isEmpty();
	}
}