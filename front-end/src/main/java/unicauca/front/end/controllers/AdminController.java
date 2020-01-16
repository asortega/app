package unicauca.front.end.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.github.javafaker.Faker;
import com.google.gson.Gson;

import controladores.EmbargosController;
import enumeraciones.Ciudad;
import enumeraciones.Departamento;
import enumeraciones.TipoAutoridad;
import enumeraciones.TipoEmbargo;
import enumeraciones.TipoIdentificacion;
import modelo.Autoridad;
import modelo.Demandado;
import modelo.Demandante;
import modelo.EmbargoCoactivo;
import modelo.EmbargoJudicial;
import modelo.Usuario;
import simulacion.SimulacionCasos;
import unicauca.front.end.service.Consulta;

@Controller
@RequestMapping("/admin")
public class AdminController {

	private Faker faker;
	private Usuario usuario;
	private String boton;

	@GetMapping("/app")
	public String main(Model model) {
		Autoridad autoridad = new Autoridad();
		autoridad.getUsuarios().add(new Usuario());
		model.addAttribute("titulo", "App");
		model.addAttribute("form", "Formulario");
		model.addAttribute("autoridad", autoridad);
		boton = "all";
		model.addAttribute("boton", boton);

		return "admin/main";
	}

	@RequestMapping(value = "/app/form", method = RequestMethod.POST, params = "action=crear")
	public String crearAut(@ModelAttribute(name = "autoridad") Autoridad autoridad, Model model,
			RedirectAttributes flash) {
		boolean band = false;
		int tam = autoridad.getUsuarios().size();
		if (checkAutoridad(autoridad)) {
			if (checkUsuario(autoridad.getUsuarios().get(tam - 1))) {
				// Consultar si no existe el usuario a agregar en la Blockchain(No esta)
				if (BackEndController.obtenerUsuario(autoridad.getUsuarios().get(tam - 1).getUsername()) == null) {
					if (autoridad.getUsuarios().get(tam - 1).getConfirmPassword()
							.equals(autoridad.getUsuarios().get(tam - 1).getPassword())) {
						// Crear nuevo usuario en la Blockchain(no esta)
						autoridad.getUsuarios().get(tam - 1).setOwnedBy(autoridad.getIdAutoridad());
						autoridad.getUsuarios().get(tam - 1).getRoles().add("ADMIN");
						EmbargosController.guardarUsuario(autoridad.getUsuarios().get(tam - 1));
						band = true;
					} else {
						flash.addFlashAttribute("error",
								"No se puede Crear Autoridad,Contraseñas de Usuario no coinciden");
						band = false;
					}
				} else {
					flash.addFlashAttribute("error", "No se puede Crear Autoridad,Nombre de Usuario ya existe");
					autoridad.getUsuarios().get(tam - 1).setUsername(null);
					band = false;
				}
			} else {
				flash.addFlashAttribute("error", "No se puede Crear Autoridad,Por favor llenar el formulario Usuario");
				band = false;
			}

		} else {
			flash.addFlashAttribute("error", "No se puede Crear Autoridad,Por favor llenar el formulario");

			band = false;
		}
		if (band == true) {
			flash.addFlashAttribute("success", "Autoridad creada con exito");
			//EmbargosController.guardarAutoridad(autoridad);
		}

		flash.addFlashAttribute("autoridad", autoridad);
		boton = "all";
		flash.addFlashAttribute("boton", boton);
		return "redirect:/admin/app/crear";

	}
	/*
	 	System.out.println(autoridad.getUsuarios().get(tam - 1).getIdentificacion());
		System.out.println(autoridad.getUsuarios().get(tam - 1).getTipoIdentificacion());
		System.out.println(autoridad.getUsuarios().get(tam - 1).getNombres());
		System.out.println(autoridad.getUsuarios().get(tam - 1).getApellidos());
		System.out.println(autoridad.getUsuarios().get(tam - 1).getUsername());
		System.out.println(autoridad.getUsuarios().get(tam - 1).getPassword());
		System.out.println(autoridad.getUsuarios().get(tam - 1).getConfirmPassword());
	 
	 */
	
	
	/*
	 * @RequestMapping(value = "/app/form", method = RequestMethod.POST, params =
	 * "action=crear") public String crearAut(@Valid @ModelAttribute(name =
	 * "autoridad") Autoridad autoridad,Errors errors, Model model,
	 * RedirectAttributes flash) { if(errors.hasErrors()) {
	 * model.addAttribute("titulo", "App"); model.addAttribute("form",
	 * "Formulario"); boton = "all"; model.addAttribute("boton", boton); return
	 * "admin/main"; }else { flash.addFlashAttribute("success",
	 * "Autoridad creada con exito"); return "redirect:/admin/app/crear"; } }
	 */

	/*
	 * public String checkAll(Autoridad autoridad) {
	 * 
	 * int tam = autoridad.getUsuarios().size(); String msj=null; if
	 * (checkAutoridad(autoridad)) { if
	 * (checkUsuario(autoridad.getUsuarios().get(tam - 1))) { if
	 * (usuarioDao.findByUsername(autoridad.getUsuarios().get(tam -
	 * 1).getUsername()) == null) { if (autoridad.getUsuarios().get(tam -
	 * 1).getConfirmPassword() .equals(autoridad.getUsuarios().get(tam -
	 * 1).getPassword())) { msj="ok"; } else {
	 * msj="No se puede Crear Autoridad,Contraseñas de Usuario no coinciden"; } }
	 * else {
	 * 
	 * //msj="error""No se puede Crear Autoridad,Nombre de Usuario ya existe";
	 * 
	 * } return mensaje; }
	 */

	@GetMapping("/app/crear")
	public String system(Model model) {
		model.addAttribute("titulo", "App");
		model.addAttribute("form", "Formulario");
		return "admin/main";
	}

	@RequestMapping(value = "/app/form", method = RequestMethod.POST, params = "action=agregar")
	public String agregar(Model model, @ModelAttribute(name = "autoridad") Autoridad autoridad,
			RedirectAttributes flash) {
		boton = "all";
		flash.addFlashAttribute("autoridad", autoridad);
		flash.addFlashAttribute("boton", boton);
		return "redirect:/admin/app/agregar";
	}

	@RequestMapping(value = "/app/form", method = RequestMethod.POST, params = "action=inactualizar")
	public String upagregar(@ModelAttribute(name = "autoridad") Autoridad autoridad, RedirectAttributes flash) {
		boton = "onactualizar";
		autoridad.getUsuarios().add(new Usuario());
		flash.addFlashAttribute("autoridad", autoridad);
		flash.addFlashAttribute("boton", boton);
		return "redirect:/admin/app/crear";
	}

	@RequestMapping(value = "/app/form", method = RequestMethod.POST, params = "action=onagregar")
	public String onagregar(Model model, @ModelAttribute(name = "autoridad") Autoridad autoridad,
			RedirectAttributes flash) {
		boton = "onactualizar";
		flash.addFlashAttribute("autoridad", autoridad);
		flash.addFlashAttribute("boton", boton);
		return "redirect:/admin/app/agregar";
	}

	@GetMapping("/app/agregar")
	public String add(Model model, @ModelAttribute(name = "autoridad") Autoridad autoridad,
			@ModelAttribute(name = "boton") String boton, RedirectAttributes flash) {

		int tam = autoridad.getUsuarios().size();

		if (checkUsuario(autoridad.getUsuarios().get(tam - 1))) {
			if (BackEndController.obtenerUsuario(autoridad.getUsuarios().get(tam - 1).getUsername()) == null) {
				if (autoridad.getUsuarios().get(tam - 1).getConfirmPassword()
						.equals(autoridad.getUsuarios().get(tam - 1).getPassword())) {
					EmbargosController.guardarUsuario(autoridad.getUsuarios().get(tam - 1));
					autoridad.getUsuarios().add(new Usuario());
				} else {
					flash.addFlashAttribute("error", "No se puede Agregar Usuario,Contraseñas no coinciden");
					autoridad.getUsuarios().get(tam - 1).setPassword(null);
					autoridad.getUsuarios().get(tam - 1).setConfirmPassword(null);
				}
			} else {
				flash.addFlashAttribute("error", "No se puede Agregar Usuario,Nombre de Usuario ya existe");
				autoridad.getUsuarios().get(tam - 1).setUsername(null);
			}
		} else {
			flash.addFlashAttribute("error", "No se puede Agregar Usuario,Por favor llenar el formulario");
			autoridad.setIdAutoridad(null);

		}
		flash.addFlashAttribute("autoridad", autoridad);
		flash.addFlashAttribute("boton", boton);
		return "redirect:/admin/app/crear";
	}

	@RequestMapping(value = "/app/form", method = RequestMethod.POST, params = "action=consultar")
	public String consultaAut(@ModelAttribute(name = "autoridad") Autoridad autoridad, RedirectAttributes flash) {
		flash.addFlashAttribute("autoridad", autoridad);
		return "redirect:/admin/app/consulta";
		// return "redirect:/admin/app/consultar";
	}

	@GetMapping("/app/consulta")
	public String loadConsulta(@ModelAttribute(name = "autoridad") Autoridad autoridad, Model model,
			RedirectAttributes flash) throws JSONException {

		Consulta selector = new Consulta();
		if (!consulta(autoridad).isEmpty()) {
			selector.setSelector(consulta(autoridad));
			Gson gson = new Gson();
			String consulta = gson.toJson(selector);
			System.out.println("Consulta: " + consulta);
			
			String mensaje = "{\"key\":1,\"Record\":{\"idAutoridad\":\"AUT1\",\"nombre\":\"JUZGADO1\",\"tipoAutoridad\":\"JUDICIAL\",\"direccion\":\"Calle 2 # 489\",\"departamento\":\"CAUCA\",\"ciudad\":\"POPAYÁN\",\"usuarios\":[{\"identificacion\":789,\"tipoIdentificacion\":\"NATURAL\",\"nombres\":\"santiago\",\"apellidos\":\"ortega\",\"username\":\"as\"},{\"identificacion\":678,\"tipoIdentificacion\":\"NATURAL\",\"nombres\":\"carlos\",\"apellidos\":\"ruiz\",\"username\":\"cr\"}]}},{\"key\":2,\"Record\":{\"idAutoridad\":\"AUT2\",\"nombre\":\"JUZGADO2\",\"tipoAutoridad\":\"JUDICIAL\",\"direccion\":\"Calle 12 # 459\",\"departamento\":\"CAUCA\",\"ciudad\":\"POPAYÁN\",\"usuarios\":[{\"identificacion\":098,\"tipoIdentificacion\":\"NATURAL\",\"nombres\":\"daniel\",\"apellidos\":\"agredo\",\"username\":\"dg\"},{\"identificacion\":430,\"tipoIdentificacion\":\"NATURAL\",\"nombres\":\"juan\",\"apellidos\":\"pizo\",\"username\":\"cr\"}]}}";
			
			// String mensaje = EmbargosController.consultaGeneral(consulta);
			System.out.println("Mensaje: " + mensaje);
			ArrayList<Autoridad> autoridades = new ArrayList<Autoridad>();
			mensaje = "[" + mensaje + "]";
			JSONArray myjson = new JSONArray(mensaje);
			for (int i = 0; i < myjson.length(); i++) {
				JSONObject jsonRecord = myjson.getJSONObject(i).getJSONObject("Record");
				autoridades.add(jsontoObject(jsonRecord));
			}
			model.addAttribute("titulo", "Consulta");
			model.addAttribute("form", "Consultas");
			model.addAttribute("autoridades", autoridades);
			boton = "actualizar";
			model.addAttribute("boton", boton);
			return "admin/consulta";
		} else {
			flash.addFlashAttribute("warning", "No se puede Consultar, Por favor ingresar el campo a consultar");
			return "redirect:/admin/app";
		}

		/*
		 * ArrayList<Autoridad> autoridades = new ArrayList<Autoridad>();
		 * 
		 * for (int i = 0; i < 2; i++) { Autoridad prueba = create();
		 * autoridades.add(prueba); } model.addAttribute("titulo", "Consulta");
		 * model.addAttribute("form", "Consultas"); model.addAttribute("autoridades",
		 * autoridades); boton = "actualizar"; model.addAttribute("boton", boton);
		 * 
		 * return "admin/consulta";
		 */
	}

	@RequestMapping(value = "/app/form", method = RequestMethod.POST, params = "action=actualizar")
	public String actualizar(Model model, @ModelAttribute(name = "autoridad") Autoridad autoridad,
			RedirectAttributes flash) {

		// Buscar autoridad por el id
		Autoridad autoridadnew = BackEndController.obtenerAutoridad(autoridad.getIdAutoridad());
		// Autoridad autoridadnew = create();
		autoridad.setHabilitado(true);
		model.addAttribute("titulo", "Actualizar");
		model.addAttribute("form", "Formulario");
		model.addAttribute("autoridad", autoridadnew);
		boton = "inactualizar";
		model.addAttribute("boton", boton);
		// flash.addFlashAttribute("boton2", "activar");
		return "admin/main";
	}

	@RequestMapping(value = "/app/form", method = RequestMethod.POST, params = "action=onactualizar")
	public String actualizarOn(Model model, @ModelAttribute(name = "autoridad") Autoridad autoridad,
			RedirectAttributes flash) {

		boolean band = false;
		int tam = autoridad.getUsuarios().size();
		if (checkAutoridad(autoridad)) {

			for (Usuario usuario : autoridad.getUsuarios()) {
				if (checkUsuario(usuario)) { // Consultar si no existe el usuario a agregar en la Blockchain(No esta)
					if (BackEndController.obtenerUsuario(usuario.getUsername()) == null) {
						if (usuario.getPassword().equals(usuario.getConfirmPassword())) {
							// Crear nuevo usuario en la Blockchain(no esta)
							EmbargosController.guardarUsuario(autoridad.getUsuarios().get(tam - 1));
							band = true;
						} else {
							flash.addFlashAttribute("error",
									"No se puede Crear Autoridad,Contraseñas de Usuario no coinciden");
							band = false;
						}
					} else {
						// Buscar usuario en la block y actualizar
						// UsuarioBD usuarioBD=usuarioDao.findByUsername(usuario.getUsername());
						// usuarioBD.setPassword(usuario.getPassword()); band = true; } } else {
						flash.addFlashAttribute("error",
								"No se puede Crear Autoridad,Por favor llenar el formulario Usuario");
						band = false;
					}
				} else {
					flash.addFlashAttribute("error", "No se puede Crear Autoridad,Por favor llenar el formulario");
					band = false;
				}
			}
		}

			if (band == true) {
				flash.addFlashAttribute("success", "Autoridad actualizada con exito");
				boton = "actualizar";
				flash.addFlashAttribute("autoridad", autoridad);
				flash.addFlashAttribute("boton", boton);
				return "redirect:/admin/app/consulta";
				// EmbargosController.guardarAutoridad(autoridad);
			} else {
				flash.addFlashAttribute("autoridad", autoridad);
				return "redirect:/admin/app/actualizar";
			}
		

		/*
		 * ArrayList<Autoridad> autoridades = new ArrayList<Autoridad>();
		 * 
		 * for (int i = 0; i < 2; i++) { Autoridad prueba = create();
		 * autoridades.add(prueba); } boton = "actualizar";
		 * flash.addFlashAttribute("autoridades", autoridades);
		 * flash.addFlashAttribute("boton", boton); return
		 * "redirect:/admin/app/consulta";
		 */
	}

	@GetMapping("/app/actualizar")
	public String loadActualizar(Model model) {
		model.addAttribute("titulo", "Actualizar");
		model.addAttribute("form", "Formulario");
		boton = "inactualizar";
		model.addAttribute("boton", boton);
		return "admin/main";
	}

	public HashMap<String, String> consulta(Autoridad autoridad) {
		HashMap<String, String> campos = new HashMap<String, String>();

		if (!autoridad.getIdAutoridad().isEmpty()) {
			campos.put("idAutoridad", autoridad.getIdAutoridad());
		} else {
			if (!autoridad.getNombre().isEmpty()) {
				campos.put("nombre", autoridad.getNombre());
			} else {
				if (autoridad.getTipoAutoridad() != null) {
					campos.put("tipoAutoridad", autoridad.getTipoAutoridad().toString());
				} else {
					if (!autoridad.getDireccion().isEmpty()) {
						campos.put("direccion", autoridad.getDireccion());
					} else {
						if (autoridad.getDepartamento() != null) {
							campos.put("departamento", autoridad.getDepartamento().toString());
						} else {
							if (autoridad.getCiudad() != null) {
								campos.put("ciudad", autoridad.getCiudad().toString());
							} else {
								if (!autoridad.getUsuarios().get(0).getIdentificacion().isEmpty()) {
									campos.put("identificacion", autoridad.getUsuarios().get(0).getIdentificacion());
								} else {
									if (autoridad.getUsuarios().get(0).getTipoIdentificacion() != null) {
										campos.put("tipoIdentificacion",
												autoridad.getUsuarios().get(0).getTipoIdentificacion().toString());
									} else {
										if (!autoridad.getUsuarios().get(0).getNombres().isEmpty()) {
											campos.put("nombres", autoridad.getUsuarios().get(0).getNombres());
										} else {
											if (!autoridad.getUsuarios().get(0).getApellidos().isEmpty()) {
												campos.put("apellidos", autoridad.getUsuarios().get(0).getApellidos());
											} else {
												if (!autoridad.getUsuarios().get(0).getUsername().isEmpty()) {
													campos.put("username",
															autoridad.getUsuarios().get(0).getUsername());
												}
											}
										}
									}
								}
							}

						}
					}
				}
			}
		}
		return campos;
	}

	public Autoridad jsontoObject(JSONObject jsonRecord) throws JSONException {
		Autoridad autoridad = new Autoridad();

		if (jsonRecord.has("idAutoridad")) {
			autoridad.setIdAutoridad(jsonRecord.getString("idAutoridad"));
		}
		if (jsonRecord.has("nombre")) {
			autoridad.setNombre(jsonRecord.getString("nombre"));
		}
		if (jsonRecord.has("tipoAutoridad")) {
			autoridad.setTipoAutoridad(TipoAutoridad.valueOf(jsonRecord.getString("tipoAutoridad")));
		}
		if (jsonRecord.has("direccion")) {
			autoridad.setDireccion(jsonRecord.getString("direccion"));
		}
		if (jsonRecord.has("departamento")) {
			autoridad.setDepartamento(Departamento.valueOf(jsonRecord.getString("departamento")));
		}
		if (jsonRecord.has("ciudad")) {
			autoridad.setCiudad(Ciudad.valueOf(jsonRecord.getString("ciudad")));
		}
		ArrayList<Usuario> usuarios = new ArrayList<Usuario>();
		if (jsonRecord.has("usuarios")) {
			JSONArray jsonUsuarios = jsonRecord.getJSONArray("usuarios");

			for (int k = 0; k < jsonUsuarios.length(); k++) {
				Usuario usuario = new Usuario();
				usuario.setIdentificacion(jsonUsuarios.getJSONObject(k).getString("identificacion"));
				usuario.setTipoIdentificacion(
						TipoIdentificacion.valueOf(jsonUsuarios.getJSONObject(k).getString("tipoIdentificacion")));
				usuario.setNombres(jsonUsuarios.getJSONObject(k).getString("nombres"));
				usuario.setApellidos(jsonUsuarios.getJSONObject(k).getString("apellidos"));
				usuario.setUsername(jsonUsuarios.getJSONObject(k).getString("username"));
				usuarios.add(usuario);
			}
			autoridad.setUsuarios(usuarios);
		}

		return autoridad;
	}

	public Autoridad create() {
		faker = new Faker();
		String idAutoridad = "autoridad" + ThreadLocalRandom.current().nextInt(1, 99999 + 1);
		TipoAutoridad tipoAutoridad = TipoAutoridad.values()[ThreadLocalRandom.current().nextInt(0,
				TipoAutoridad.values().length)];
		Departamento departamento = Departamento.values()[ThreadLocalRandom.current().nextInt(0,
				Departamento.values().length)];
		String nombre = faker.name().firstName();
		String direccion = faker.address().firstName();
		Ciudad ciudad = Ciudad.values()[ThreadLocalRandom.current().nextInt(0, Ciudad.values().length)];
		ArrayList<Usuario> usuarios = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			String identificacion = "usuario" + ThreadLocalRandom.current().nextInt(1, 99999 + 1);
			String username = "username" + ThreadLocalRandom.current().nextInt(1, 99999 + 1);
			TipoIdentificacion tipoIdentificacion = TipoIdentificacion.values()[ThreadLocalRandom.current().nextInt(0,
					TipoIdentificacion.values().length)];

			String nombres = faker.name().firstName();
			String apellidos = faker.name().lastName();
			ArrayList<String> roles = new ArrayList<>();
			roles.add("ADMIN");
			Usuario usuario = new Usuario(identificacion, nombres, apellidos, tipoIdentificacion, username, "123",
					"123", roles, true);
			usuarios.add(usuario);
		}
		Autoridad autoridad = new Autoridad(idAutoridad, tipoAutoridad, nombre, direccion, ciudad, departamento,
				usuarios);
		return autoridad;
	}

	public Usuario createUsuario() {
		faker = new Faker();
		String identificacion = "persona" + ThreadLocalRandom.current().nextInt(1, 99999 + 1);
		TipoIdentificacion tipoIdentificacion = TipoIdentificacion.values()[ThreadLocalRandom.current().nextInt(0,
				TipoIdentificacion.values().length)];
		String nombres = faker.name().firstName();
		String username = faker.name().username();
		String apellidos = faker.name().lastName();
		ArrayList<String> roles = new ArrayList<>();
		roles.add("ADMIN");
		boolean habilitado = true;
		Usuario usuario = new Usuario(identificacion, nombres, apellidos, tipoIdentificacion, username, roles, null,
				habilitado);
		// persona.setEstado("INACTIVO");
		return usuario;
	}

	private boolean checkAutoridad(Autoridad autoridad) {
		return !autoridad.getIdAutoridad().isEmpty() && !autoridad.getNombre().isEmpty()
				&& autoridad.getTipoAutoridad() != null && !autoridad.getDireccion().isEmpty()
				&& autoridad.getDepartamento() != null && autoridad.getCiudad() != null;
	}

	private boolean checkUsuario(Usuario usuario) {
		return !usuario.getIdentificacion().isEmpty() && usuario.getTipoIdentificacion() != null
				&& !usuario.getNombres().isEmpty() && !usuario.getApellidos().isEmpty()
				&& !usuario.getUsername().isEmpty() && !usuario.getPassword().isEmpty()
				&& !usuario.getConfirmPassword().isEmpty();
	}

	private boolean checkNullUser(Usuario usuario) {
		return usuario.getIdentificacion() != null && usuario.getTipoIdentificacion() != null
				&& usuario.getNombres() != null && usuario.getApellidos() != null && usuario.getUsername() != null
				&& usuario.getPassword() != null && usuario.getConfirmPassword() != null;
	}
	/*
	 * System.out.println("ID Autoridad: " + autoridad.getIdAutoridad());
	 * System.out.println("Nombre Autoridad: " + autoridad.getNombre());
	 * System.out.println("Direccion Autoridad: " + autoridad.getDireccion());
	 * System.out.println("Ciudad Autoridad: " + autoridad.getCiudad());
	 * System.out.println("Departamento Autoridad: " + autoridad.getDepartamento());
	 * for (Usuario usuario : autoridad.getUsuarios()) {
	 * System.out.println("Id Usuario : " + usuario.getIdentificacion());
	 * System.out.println("Nombres Usuario : " + usuario.getNombres());
	 * System.out.println("Apellidos Usuario : " + usuario.getApellidos()); }
	 * 
	 */
}