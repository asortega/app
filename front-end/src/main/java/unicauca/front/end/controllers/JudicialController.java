package unicauca.front.end.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.poi.ss.usermodel.*;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kie.api.runtime.KieSession;
import org.springframework.security.access.annotation.Secured;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.github.javafaker.Faker;
import com.google.gson.Gson;

import controladores.EmbargosController;
import enumeraciones.TipoEmbargo;
import enumeraciones.TipoIdentificacion;
import modelo.Demandado;
import modelo.Demandante;
import modelo.Embargo;
import modelo.EmbargoCoactivo;
import modelo.EmbargoJudicial;
import modelo.Intento;
import simulacion.SimulacionCasos;
import simulacion.SimulacionPasarelas;
import unicauca.front.end.service.Consulta;
import unicauca.front.end.service.Service;
import util.SessionHelper;

@Controller
@RequestMapping("/autoridad/judicial")
public class JudicialController {

	private EmbargoJudicial embargo;
	private SimulacionPasarelas simulacionPasarela;
	private SessionHelper session;
	private Service service;
	private Faker faker;

	public JudicialController() {
		simulacionPasarela = new SimulacionPasarelas();
		session = new SessionHelper();
		service = new Service();
	}

	@GetMapping("/secretario")
	public String secretario(Model model) {
		EmbargoJudicial embargoJudicial = new EmbargoJudicial();
		embargoJudicial.getDemandantes().add(new Demandante());
		embargoJudicial.getDemandados().add(new Demandado());
		model.addAttribute("titulo", "App");
		model.addAttribute("form", "Formulario");
		model.addAttribute("embargoJudicial", embargoJudicial);
		return "autoridad/judicial/secretario/main";

	}

	@PostMapping("/secretario")
	public String consJudicial(@ModelAttribute(name = "embargoJudicial") EmbargoJudicial embargoJudicial, Model model,
			RedirectAttributes flash) {
		ArrayList<EmbargoJudicial> embargos = new ArrayList<EmbargoJudicial>();

		for (int i = 0; i < 2; i++) {
			EmbargoJudicial prueba = (EmbargoJudicial) SimulacionCasos.generarEmbargoNormal();
			embargos.add(prueba);
		}
		model.addAttribute("titulo", "Consulta");
		model.addAttribute("form", "Consultas");
		model.addAttribute("id", embargoJudicial.getIdAutoridad());
		model.addAttribute("embargos", embargos);
		return "autoridad/judicial/secretario/consulta";
	}

	// ------------GESTOR-----------
	@Secured("ROLE_JUDICIAL")
	@GetMapping("/gestor")
	public String crearEmbargo(Model model) {

		embargo = new EmbargoJudicial();
		embargo.getDemandantes().add(new Demandante());
		embargo.getDemandados().add(new Demandado());
		model.addAttribute("titulo", "App");
		model.addAttribute("form", "Formulario");
		model.addAttribute("embargo", embargo);
		model.addAttribute("boton", "all");
		return "autoridad/judicial/gestor/main";
	}

	/*
	 * @GetMapping("/gestor/cargar") public String cargarEmbargo(Model model) {
	 * 
	 * embargo = (EmbargoJudicial) SimulacionCasos.generarEmbargoNormal(); //embargo
	 * = (EmbargoJudicial) generarEmbargoNormal(); model.addAttribute("titulo",
	 * "Embargo"); model.addAttribute("form", "Formulario");
	 * model.addAttribute("embargo", embargo); model.addAttribute("boton", "all");
	 * return "autoridad/judicial/gestor/main"; }
	 */

	@RequestMapping(value = "/gestor/form", method = RequestMethod.POST, params = "action=cargar")
	public String cargarEmbargo(@RequestParam("file") MultipartFile archivo, Model model, RedirectAttributes flash)
			throws IOException {
		boolean band = false;
		EmbargoJudicial embargoJudicial = new EmbargoJudicial();
		if (!archivo.isEmpty()) {
			// System.out.println("Nombre Archivo: "+archivo.getOriginalFilename());
			FileInputStream file = new FileInputStream(new File(archivo.getOriginalFilename()));
			XSSFWorkbook workbook = new XSSFWorkbook(file);
			Sheet sheet = workbook.getSheetAt(0);
			for (int i = sheet.getFirstRowNum() + 2; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				Demandante demandante = new Demandante();
				Demandado demandado = new Demandado();
				embargoJudicial = asignarEmbargo(embargoJudicial, demandante, demandado, row, i);
			}
			workbook.close();
			file.close();
			band = true;
		}
		if (band == true) {
			model.addAttribute("titulo", "Embargo");
			model.addAttribute("form", "Formulario");
			model.addAttribute("embargo", embargoJudicial);
			model.addAttribute("boton", "all");
			return "autoridad/judicial/gestor/main";
		} else {
			flash.addFlashAttribute("warning", "Por favor, elegir archivo a cargar");
			return "redirect:/autoridad/judicial/gestor";
		}

	}

	public EmbargoJudicial asignarEmbargo(EmbargoJudicial embargoJudicial, Demandante demandante, Demandado demandado,
			Row row, int i) {
		Iterator<Cell> cellIterator = row.cellIterator();
		while (cellIterator.hasNext()) {
			Cell ce = cellIterator.next();
			int columnIndex = ce.getColumnIndex();
			if (ce.getCellType() != CellType.BLANK) {
				switch (columnIndex) {
				case 0:
					embargoJudicial.setNumProceso(ce.getStringCellValue());
					break;
				case 1:
					embargoJudicial.setNumOficio(ce.getStringCellValue());
					break;
				case 2:
					embargoJudicial.setFechaOficio(ce.getLocalDateTimeCellValue().toLocalDate());
					break;
				case 3:
					embargoJudicial.setTipoEmbargo(TipoEmbargo.valueOf(ce.getStringCellValue()));
					break;
				case 4:
					embargoJudicial.setMontoAEmbargar(new BigDecimal(ce.getNumericCellValue()));
					break;
				case 5:
					embargoJudicial.setNumCuentaAgrario(ce.getStringCellValue());
					break;
				case 6:
					demandante.setIdentificacion(ce.getStringCellValue());
					break;
				case 7:
					demandante.setTipoIdentificacion(TipoIdentificacion.valueOf(ce.getStringCellValue()));
					break;
				case 8:
					demandante.setNombres(ce.getStringCellValue());
					break;
				case 9:
					demandante.setApellidos(ce.getStringCellValue());
					break;
				case 10:
					demandado.setIdentificacion(ce.getStringCellValue());
					break;
				case 11:
					demandado.setTipoIdentificacion(TipoIdentificacion.valueOf(ce.getStringCellValue()));
					break;
				case 12:
					demandado.setNombres(ce.getStringCellValue());
					break;
				case 13:
					demandado.setApellidos(ce.getStringCellValue());
					break;
				case 14:
					demandado.setMontoAEmbargar(new BigDecimal(ce.getNumericCellValue()));
					break;
				default:
					break;
				}
			}
		}
		embargoJudicial.getDemandantes().add(demandante);
		embargoJudicial.getDemandados().add(demandado);
		return embargoJudicial;
	}

	@RequestMapping(value = "/gestor/form", method = RequestMethod.POST, params = "action=demandante")
	public String addDemandante(@ModelAttribute(name = "embargo") EmbargoJudicial embargo, Model model) {
		embargo.getDemandantes().add(new Demandante());
		model.addAttribute("titulo", "Embargo");
		model.addAttribute("form", "Formulario");
		model.addAttribute("id", embargo.getIdAutoridad());
		model.addAttribute("boton", "all");
		return "autoridad/judicial/gestor/main";
	}

	@RequestMapping(value = "/gestor/form", method = RequestMethod.POST, params = "action=demandado")
	public String addDemandado(@ModelAttribute(name = "embargo") EmbargoJudicial embargo, Model model) {
		embargo.getDemandados().add(new Demandado());
		model.addAttribute("titulo", "Embargo");
		model.addAttribute("form", "Formulario");
		model.addAttribute("boton", "all");
		return "autoridad/judicial/gestor/main";
	}

	@RequestMapping(value = "/gestor/form", method = RequestMethod.POST, params = "action=aplicar")
	public String aplicar(@ModelAttribute(name = "embargo") EmbargoJudicial embargo, Model model,
			RedirectAttributes flash) {
		try {
			KieSession sessionStatefull = session.obtenerSesion();
			sessionStatefull.insert(embargo);
			sessionStatefull.fireAllRules();
			session.cerrarSesion(sessionStatefull);
			String mensajePasarela = simulacionPasarela.llamarPasarelas(embargo.getDemandados());
			model.addAttribute("titulo", "Aplicar");
			model.addAttribute("form", "Resultado");
			model.addAttribute("embargo", embargo);
			model.addAttribute("mensajePasarela", mensajePasarela);
			model.addAttribute("mensaje", service.imprimir(embargo));
			model.addAttribute("boton", "all");
			return "autoridad/judicial/gestor/output";

		} catch (NullPointerException e) {
			flash.addFlashAttribute("warning", "No se puede Aplicar,Por favor llenar el formulario");
			return "redirect:/autoridad/judicial/gestor";
		}
	}

	@PostMapping("/gestor/aplicar")
	public String reaplicar(@ModelAttribute(name = "embargo") EmbargoJudicial embargo, Model model,
			RedirectAttributes flash) {

		System.out.println("Id Autoridad:" + embargo.getIdAutoridad());
		System.out.println("Num proceso:" + embargo.getNumProceso());
		System.out.println("Fecha Oficio:" + embargo.getFechaOficio());
		System.out.println("Tipo Embargo:" + embargo.getTipoEmbargo());
		System.out.println("Num Cuenta Agrario:" + embargo.getNumCuentaAgrario());
		for (Demandante demandante : embargo.getDemandantes()) {
			System.out.println("Id Demandante:" + demandante.getIdentificacion());
			System.out.println("Nombres demandante:" + demandante.getNombres());
		}
		for (Demandado demandado : embargo.getDemandados()) {
			System.out.println("Id Demandado:" + demandado.getIdentificacion());
			System.out.println("Nombres demandado:" + demandado.getNombres());
		}
		model.addAttribute("mensajePasarela", "Hola Mundo");
		model.addAttribute("boton", "consulta");
		return "autoridad/judicial/gestor/output";
	}

	@RequestMapping(value = "/gestor/aplicar/{boton}/{mensajePasarela}", method = RequestMethod.POST, params = "action=siaplicar")
	public String aplicarMedida(@ModelAttribute(name = "embargo") EmbargoJudicial embargo,
			@PathVariable(value = "mensajePasarela") String mensajePasarela,
			@PathVariable(value = "boton") String boton, Model model) {
		System.out.println("Boton SI Aplicar:" + boton);
		System.out.println("Msj Pasarela:" + mensajePasarela);
		System.out.println("Num proceso:" + embargo.getNumProceso());
		System.out.println("Fecha Oficio:" + embargo.getFechaOficio());
		System.out.println("Tipo Embargo:" + embargo.getTipoEmbargo());

		/*
		 * for (Demandado demandado : embargo.getDemandados()) { ArrayList<Intento>
		 * intentos = new ArrayList<>(); Intento intento = new Intento(LocalDate.now(),
		 * true, mensajePasarela, demandado.getCuentas()); intentos.add(intento);
		 * demandado.setIntentos(intentos); } model.addAttribute("titulo", "App");
		 * model.addAttribute("form", "Formulario"); model.addAttribute("embargo",
		 * embargo); model.addAttribute("boton", "all");
		 * embargo.setEmbargoProcesado(true); Authentication authentication =
		 * SecurityContextHolder.getContext().getAuthentication(); String username =
		 * authentication.getName(); embargo.setIdAutoridad(username);
		 */
		// EmbargosController.guardarEmbargo(embargo);
		// return "redirect:/autoridad/judicial/gestor";*/
		return "autoridad/judicial/gestor/msj";
	}

	@RequestMapping(value = "/gestor/aplicar/{boton}/{mensajePasarela}", method = RequestMethod.POST, params = "action=noaplicar")
	public String noAplicarMedida(@ModelAttribute(name = "embargo") Embargo embargo,
			@PathVariable(value = "mensajePasarela") String mensajePasarela, Model model, RedirectAttributes flash) {
		flash.addFlashAttribute("success", "Embargo NO aplicado");
		for (Demandado demandado : embargo.getDemandados()) {
			ArrayList<Intento> intentos = new ArrayList<>();
			Intento intento = new Intento(LocalDate.now(), false, mensajePasarela, demandado.getCuentas());
			intentos.add(intento);
			demandado.setIntentos(intentos);
		}
		model.addAttribute("titulo", "App");
		model.addAttribute("form", "Formulario");
		model.addAttribute("id", embargo.getIdAutoridad());
		embargo.setEmbargoProcesado(false);
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();
		embargo.setIdAutoridad(username);
		// EmbargosController.guardarEmbargo(embargo);
		return "redirect:/autoridad/judicial/gestor";
	}

	@RequestMapping(value = "/gestor/form", method = RequestMethod.POST, params = "action=consultar")
	public String consultar(@ModelAttribute(name = "embargo") EmbargoJudicial embargo, Model model,
			RedirectAttributes flash) throws JSONException {

		Consulta selector = new Consulta();
		if (!consulta(embargo).isEmpty()) {
			selector.setSelector(consulta(embargo));
			Gson gson = new Gson();
			String consulta = gson.toJson(selector);
			System.out.println("Consulta: " + consulta);
			String mensaje = EmbargosController.consultaGeneral(consulta);
			System.out.println("Mensaje: " + mensaje);
			ArrayList<EmbargoJudicial> embargos = new ArrayList<EmbargoJudicial>();
			mensaje = "[" + mensaje + "]";
			JSONArray myjson = new JSONArray(mensaje);
			for (int i = 0; i < myjson.length(); i++) {
				JSONObject jsonRecord = myjson.getJSONObject(i).getJSONObject("Record");
				embargos.add(jsontoObject(jsonRecord));
			}
			model.addAttribute("titulo", "Consulta");
			model.addAttribute("form", "Consultas");
			model.addAttribute("embargos", embargos);
			model.addAttribute("boton", "consulta");
			return "autoridad/judicial/gestor/consulta";
		} else {
			flash.addFlashAttribute("warning", "No se puede Consultar, Por favor ingresar el campo a consultar");
			return "redirect:/autoridad/judicial/gestor";
		}
	}

	@PostMapping("/gestor/msj/{boton}")
	public String inmsj(@ModelAttribute(name = "embargo") EmbargoJudicial embargo, RedirectAttributes flash,
			@PathVariable(value = "boton") String boton) {
		flash.addFlashAttribute("embargo", embargo);
		flash.addFlashAttribute("boton", boton);
		flash.addFlashAttribute("success", "Embargo aplicado");
		if (boton.equals("all")) {
			return "redirect:/autoridad/judicial/gestor/main";
		} else {
			return "redirect:/autoridad/judicial/gestor/consulta";
		}
	}

	@GetMapping("/gestor/main")
	public String outmsj(Model model) {

		System.out.println("Id Autoridad:" + embargo.getIdAutoridad());
		System.out.println("Num proceso:" + embargo.getNumProceso());
		System.out.println("Fecha Oficio:" + embargo.getFechaOficio());
		System.out.println("Tipo Embargo:" + embargo.getTipoEmbargo());
		System.out.println("Num Cuenta Agrario:" + embargo.getNumCuentaAgrario());
		for (Demandado demandado : embargo.getDemandados()) {
			System.out.println("Id Demandado:" + demandado.getIdentificacion());
			System.out.println("Nombres demandado:" + demandado.getNombres());
		}
		model.addAttribute("titulo", "App");
		model.addAttribute("form", "Formulario");
		return "autoridad/judicial/gestor/main";
	}

	@GetMapping("/gestor/consulta")
	public String outconsulta(Model model, @ModelAttribute(name = "embargo") EmbargoJudicial embargo) {

		System.out.println("Id Autoridad:" + embargo.getIdAutoridad());
		System.out.println("Num proceso:" + embargo.getNumProceso());
		System.out.println("Fecha Oficio:" + embargo.getFechaOficio());
		System.out.println("Tipo Embargo:" + embargo.getTipoEmbargo());
		System.out.println("Num Cuenta Agrario:" + embargo.getNumCuentaAgrario());
		ArrayList<EmbargoJudicial> embargos = new ArrayList<EmbargoJudicial>();
		EmbargoJudicial prueba = (EmbargoJudicial) SimulacionCasos.generarEmbargoNormal();
		embargos.add(prueba);
		embargo.setEmbargoProcesado(true);
		embargos.add(embargo);
		model.addAttribute("titulo", "App");
		model.addAttribute("form", "Formulario");
		model.addAttribute("embargos", embargos);
		return "autoridad/judicial/gestor/consulta";
	}

	public ArrayList<EmbargoJudicial> getAllEmbargos() {
		ArrayList<EmbargoJudicial> embargos = new ArrayList<EmbargoJudicial>();
		for (int i = 0; i < 2; i++) {
			EmbargoJudicial prueba = (EmbargoJudicial) SimulacionCasos.generarEmbargoNormal();
			embargos.add(prueba);
		}
		return embargos;
	}

	public boolean isValid(EmbargoJudicial embargoJudicial) {
		return !embargoJudicial.getIdAutoridad().isEmpty() && !embargoJudicial.getNumProceso().isEmpty()
				&& !embargoJudicial.getNumOficio().isEmpty() && embargoJudicial.getFechaOficio() != null
				&& embargoJudicial.getTipoEmbargo() != null && embargoJudicial.getMontoAEmbargar() != null
				&& !embargoJudicial.getNumCuentaAgrario().isEmpty()
				&& !embargoJudicial.getDemandantes().get(0).getIdentificacion().isEmpty()
				&& embargoJudicial.getDemandantes().get(0).getTipoIdentificacion() != null
				&& !embargoJudicial.getDemandantes().get(0).getNombres().isEmpty()
				&& !embargoJudicial.getDemandantes().get(0).getApellidos().isEmpty()
				&& !embargoJudicial.getDemandados().get(0).getIdentificacion().isEmpty()
				&& embargoJudicial.getDemandados().get(0).getTipoIdentificacion() != null
				&& !embargoJudicial.getDemandados().get(0).getNombres().isEmpty()
				&& !embargoJudicial.getDemandados().get(0).getApellidos().isEmpty()
				&& embargoJudicial.getDemandados().get(0).getMontoAEmbargar() != null;
	}

	public HashMap<String, String> consulta(EmbargoJudicial embargo) {
		HashMap<String, String> campos = new HashMap<String, String>();

		if (!embargo.getNumProceso().isEmpty()) {
			campos.put("numProceso", embargo.getNumProceso());
		} else {
			if (!embargo.getNumOficio().isEmpty()) {
				campos.put("numOficio", embargo.getNumOficio());
			} else {
				if (embargo.getFechaOficio() != null) {
					campos.put("fechaOficio", embargo.getFechaOficio().toString());
				} else {
					if (embargo.getTipoEmbargo() != null) {
						campos.put("tipoEmbargo", embargo.getTipoEmbargo().toString());
					} else {
						if (embargo.getMontoAEmbargar() != null) {
							campos.put("montoAEmbargar", embargo.getMontoAEmbargar().toString());
						} else {
							if (!embargo.getNumCuentaAgrario().isEmpty()) {
								campos.put("numCuentaAgrario", embargo.getNumCuentaAgrario());
							} else {
								if (!embargo.getDemandantes().get(0).getIdentificacion().isEmpty()) {
									campos.put("identificacion", embargo.getDemandantes().get(0).getIdentificacion());
								} else {
									if (embargo.getDemandantes().get(0).getTipoIdentificacion() != null) {
										campos.put("tipoIdentificacion",
												embargo.getDemandantes().get(0).getTipoIdentificacion().toString());
									} else {
										if (!embargo.getDemandantes().get(0).getNombres().isEmpty()) {
											campos.put("nombres", embargo.getDemandantes().get(0).getNombres());
										} else {
											if (!embargo.getDemandantes().get(0).getApellidos().isEmpty()) {
												campos.put("apellidos", embargo.getDemandantes().get(0).getApellidos());
											} else {
												if (!embargo.getDemandados().get(0).getIdentificacion().isEmpty()) {
													campos.put("identificacion",
															embargo.getDemandados().get(0).getIdentificacion());
												} else {
													if (embargo.getDemandados().get(0)
															.getTipoIdentificacion() != null) {
														campos.put("tipoIdentificacion", embargo.getDemandados().get(0)
																.getTipoIdentificacion().toString());
													} else {
														if (!embargo.getDemandados().get(0).getNombres().isEmpty()) {
															campos.put("nombres",
																	embargo.getDemandados().get(0).getNombres());
														} else {
															if (!embargo.getDemandados().get(0).getApellidos()
																	.isEmpty()) {
																campos.put("apellidos",
																		embargo.getDemandados().get(0).getApellidos());
															} else {
																if (embargo.getDemandados().get(0)
																		.getMontoAEmbargar() != null) {
																	campos.put("montoAEmbargar", embargo.getDemandados()
																			.get(0).getMontoAEmbargar().toString());
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
					}
				}
			}
		}
		return campos;

	}

	public EmbargoJudicial jsontoObject(JSONObject jsonRecord) throws JSONException {
		EmbargoJudicial embargoJudicial = new EmbargoJudicial();

		if (jsonRecord.has("numProceso")) {
			embargoJudicial.setNumProceso(jsonRecord.getString("numProceso"));
		}
		if (jsonRecord.has("numOficio")) {
			embargoJudicial.setNumOficio(jsonRecord.getString("numOficio"));
		}
		if (jsonRecord.has("fechaOficio")) {
			JSONObject jsonFecha = jsonRecord.getJSONObject("fechaOficio");
			LocalDate localDate = LocalDate.of(Integer.parseInt(jsonFecha.getString("year")),
					Integer.parseInt(jsonFecha.getString("month")), Integer.parseInt(jsonFecha.getString("day")));
			embargoJudicial.setFechaOficio(localDate);
		}
		if (jsonRecord.has("tipoEmbargo")) {
			embargoJudicial.setTipoEmbargo(TipoEmbargo.valueOf(jsonRecord.getString("tipoEmbargo")));
		}
		if (jsonRecord.has("montoAEmbargar")) {
			embargoJudicial.setMontoAEmbargar(new BigDecimal(jsonRecord.getString("montoAEmbargar")));
		}
		if (jsonRecord.has("numCuentaAgrario")) {
			embargoJudicial.setNumCuentaAgrario(jsonRecord.getString("numCuentaAgrario"));
		}
		ArrayList<Demandante> demandantes = new ArrayList<Demandante>();
		if (jsonRecord.has("demandantes")) {
			JSONArray jsonDemandantes = jsonRecord.getJSONArray("demandantes");

			for (int k = 0; k < jsonDemandantes.length(); k++) {
				Demandante demandante = new Demandante();
				demandante.setIdentificacion(jsonDemandantes.getJSONObject(k).getString("identificacion"));
				demandante.setTipoIdentificacion(
						TipoIdentificacion.valueOf(jsonDemandantes.getJSONObject(k).getString("tipoIdentificacion")));
				demandante.setNombres(jsonDemandantes.getJSONObject(k).getString("nombres"));
				demandante.setApellidos(jsonDemandantes.getJSONObject(k).getString("apellidos"));
				demandantes.add(demandante);
			}
			embargoJudicial.setDemandantes(demandantes);
		}
		ArrayList<Demandado> demandados = new ArrayList<Demandado>();
		if (jsonRecord.has("demandados")) {
			JSONArray jsonDemandados = jsonRecord.getJSONArray("demandados");

			for (int k = 0; k < jsonDemandados.length(); k++) {
				Demandado demandado = new Demandado();
				demandado.setIdentificacion(jsonDemandados.getJSONObject(k).getString("identificacion"));
				demandado.setTipoIdentificacion(
						TipoIdentificacion.valueOf(jsonDemandados.getJSONObject(k).getString("tipoIdentificacion")));
				demandado.setNombres(jsonDemandados.getJSONObject(k).getString("nombres"));
				demandado.setApellidos(jsonDemandados.getJSONObject(k).getString("apellidos"));
				demandado
						.setMontoAEmbargar(new BigDecimal(jsonDemandados.getJSONObject(k).getString("montoAEmbargar")));
				demandados.add(demandado);
			}
			embargoJudicial.setDemandados(demandados);
		}
		return embargoJudicial;
	}
}
//String mensaje
//="{\"key\":1,\"Record\":{\"idAutoridad\":\"fong\",\"demandados\":[{\"identificacion\":789,\"nombres\":\"santiago\"},{\"identificacion\":678,\"nombres\":\"carlos\"}],\"demandantes\":[{\"identificacion\":432,\"nombres\":\"miguel\"},{\"identificacion\":543,\"nombres\":\"luis\"}]}}";
//String mensaje =
//"[{\"key\":1,\"Record\":{\"idAutoridad\":\"fong\",\"demandados\":[{\"identificacion\":789,\"nombres\":\"santiago\"},{\"identificacion\":678,\"nombres\":\"carlos\"}],\"demandantes\":[{\"identificacion\":432,\"nombres\":\"miguel\"},{\"identificacion\":543,\"nombres\":\"luis\"}]}},{\"key\":2,\"Record\":{\"idAutoridad\":\"view\",\"demandados\":[{\"identificacion\":909,\"nombres\":\"andres\"},{\"identificacion\":232,\"nombres\":\"jose\"}],\"demandantes\":[{\"identificacion\":111,\"nombres\":\"paul\"},{\"identificacion\":564,\"nombres\":\"joe\"}]}}]";
