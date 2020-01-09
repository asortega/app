package unicauca.front.end.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
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
@RequestMapping("/autoridad/coactivo")
public class CoactivoController {

	private Embargo embargo;
	private SimulacionPasarelas simulacionPasarela;
	private SessionHelper session;
	private Service service;
	private Authentication authentication;

	public CoactivoController() {
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
		
		return "autoridad/coactivo/secretario/main";

	}

	@PostMapping("/secretario/coactivo")
	public String consCoactivo(@ModelAttribute(name = "embargoCoactivo") EmbargoCoactivo embargoCoactivo, Model model,
			RedirectAttributes flash) {
		ArrayList<EmbargoCoactivo> embargos = new ArrayList<EmbargoCoactivo>();
		for (int i = 0; i < 2; i++) {
			EmbargoCoactivo prueba = (EmbargoCoactivo) SimulacionCasos.generarEmbargoDian();
			embargos.add(prueba);
		}
		model.addAttribute("titulo", "Consultar");
		model.addAttribute("form", "Consultas");
		model.addAttribute("id", embargoCoactivo.getIdAutoridad());
		model.addAttribute("embargos", embargos);
		return "autoridad/coactivo/secretario/main";
	}

	/*------------GESTOR---------*/

	@Secured("ROLE_COACTIVO")
	@GetMapping("/gestor")
	public String crearEmbargo(Model model) {
		embargo = new EmbargoCoactivo();
		embargo.getDemandados().add(new Demandado());
		model.addAttribute("titulo", "App");
		model.addAttribute("form", "Formulario");
		model.addAttribute("embargo", embargo);
		model.addAttribute("boton", "all");
		return "autoridad/coactivo/gestor/main";
	}
	
	/*
	@Secured("ROLE_COACTIVO")
	@GetMapping("/gestor/cargar")
	public String cargarEmbargo(Model model) {
		embargo = (EmbargoCoactivo) SimulacionCasos.generarEmbargoDian();
		model.addAttribute("id", embargo.getIdAutoridad());
		model.addAttribute("titulo", "Embargo");
		model.addAttribute("form", "Formulario");
		model.addAttribute("embargo", embargo);
		model.addAttribute("boton", "all");
		return "autoridad/coactivo/gestor/main";
	}*/
	
	@RequestMapping(value = "/gestor/form", method = RequestMethod.POST, params = "action=cargar")
	public String cargarEmbargo(@RequestParam("file") MultipartFile archivo, Model model, RedirectAttributes flash)
			throws IOException {
		boolean band = false;
		EmbargoCoactivo embargoCoactivo = new EmbargoCoactivo();
		if (!archivo.isEmpty()) {
			// System.out.println("Nombre Archivo: "+archivo.getOriginalFilename());
			FileInputStream file = new FileInputStream(new File(archivo.getOriginalFilename()));
			XSSFWorkbook workbook = new XSSFWorkbook(file);
			Sheet sheet = workbook.getSheetAt(0);
			for (int i = sheet.getFirstRowNum() + 2; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				Demandado demandado = new Demandado();
				embargoCoactivo = asignarEmbargo(embargoCoactivo, demandado, row, i);
			}
			workbook.close();
			file.close();
			band = true;
		}
		if (band == true) {
			model.addAttribute("titulo", "Embargo");
			model.addAttribute("form", "Formulario");
			model.addAttribute("embargo", embargoCoactivo);
			model.addAttribute("boton", "all");
			return "autoridad/coactivo/gestor/main";
		} else {
			flash.addFlashAttribute("warning", "Por favor, elegir archivo a cargar");
			return "redirect:/autoridad/coactivo/gestor";
		}

	}

	public EmbargoCoactivo asignarEmbargo(EmbargoCoactivo embargoCoactivo, Demandado demandado,
			Row row, int i) {
		Iterator<Cell> cellIterator = row.cellIterator();
		while (cellIterator.hasNext()) {
			Cell ce = cellIterator.next();
			int columnIndex = ce.getColumnIndex();
			if (ce.getCellType() != CellType.BLANK) {
				switch (columnIndex) {
				case 0:
					embargoCoactivo.setNumProceso(ce.getStringCellValue());
					break;
				case 1:
					embargoCoactivo.setNumOficio(ce.getStringCellValue());
					break;
				case 2:
					embargoCoactivo.setFechaOficio(ce.getLocalDateTimeCellValue().toLocalDate());
					break;
				case 3:
					embargoCoactivo.setTipoEmbargo(TipoEmbargo.valueOf(ce.getStringCellValue()));
					break;
				case 4:
					embargoCoactivo.setNumCuentaAgrario(ce.getStringCellValue());
					break;
				case 5:
					demandado.setIdentificacion(ce.getStringCellValue());
					break;
				case 6:
					demandado.setTipoIdentificacion(TipoIdentificacion.valueOf(ce.getStringCellValue()));
					break;
				case 7:
					demandado.setNombres(ce.getStringCellValue());
					break;
				case 8:
					demandado.setApellidos(ce.getStringCellValue());
					break;
				case 9:
					demandado.setResEmbargo(ce.getStringCellValue());
					break;
				case 10:
					demandado.setFechaResolucion(ce.getLocalDateTimeCellValue().toLocalDate());
					break;
				case 11:
					demandado.setMontoAEmbargar(new BigDecimal(ce.getNumericCellValue()));
					break;
				default:
					break;
				}
			}
		}
		embargoCoactivo.getDemandados().add(demandado);
		return embargoCoactivo;
	}

	@Secured("ROLE_COACTIVO")
	@RequestMapping(value = "/gestor/form", method = RequestMethod.POST, params = "action=demandado")
	public String addDemandado(@ModelAttribute(name = "embargo") EmbargoCoactivo embargo, Model model) {
		embargo.getDemandados().add(new Demandado());
		model.addAttribute("titulo", "Embargo");
		model.addAttribute("form", "Formulario");
		model.addAttribute("boton", "all");
		return "autoridad/coactivo/gestor/main";
	}

	@Secured("ROLE_COACTIVO")
	@RequestMapping(value = "/gestor/form", method = RequestMethod.POST, params = "action=aplicar")
	public String aplicar(@ModelAttribute(name = "embargo") EmbargoCoactivo embargo, Model model,
			RedirectAttributes flash) {
		try {
			KieSession sessionStatefull = session.obtenerSesion();
			String mensajePasarela = simulacionPasarela.llamarPasarelas(embargo.getDemandados());
			sessionStatefull.insert(embargo);
			sessionStatefull.fireAllRules();
			session.cerrarSesion(sessionStatefull);
			model.addAttribute("titulo", "Aplicar");
			model.addAttribute("form", "Resultado");
			model.addAttribute("id", embargo.getIdAutoridad());
			model.addAttribute("mensajePasarela", mensajePasarela);
			model.addAttribute("mensaje", service.imprimir(embargo));
			model.addAttribute("boton", "all");
			return "autoridad/coactivo/gestor/output";
		} catch (NullPointerException e) {
			flash.addFlashAttribute("warning", "No se puede Aplicar,Por favor llenar el formulario");
			return "redirect:/autoridad/coactivo/gestor";
		}
	}

	@PostMapping("/gestor/aplicar")
	public String reaplicar(@ModelAttribute(name = "embargo") EmbargoCoactivo embargo, Model model,
			RedirectAttributes flash) {
		for (Demandado demandado : embargo.getDemandados()) {
			System.out.println("Id Demandado:" + demandado.getIdentificacion());
			System.out.println("Nombres demandado:" + demandado.getNombres());
		}
		model.addAttribute("mensajePasarela", "Hola Mundo");
		model.addAttribute("boton", "consulta");
		return "autoridad/coactivo/gestor/output";
	}

	@Secured("ROLE_COACTIVO")
	@RequestMapping(value = "/gestor/aplicar/{boton}/{mensajePasarela}", method = RequestMethod.POST, params = "action=siaplicar")
	public String aplicarMedida(@ModelAttribute(name = "embargo") EmbargoCoactivo embargo,
			@PathVariable(value = "mensajePasarela") String mensajePasarela,
			@PathVariable(value = "boton") String boton, Model model, RedirectAttributes flash) {
		flash.addFlashAttribute("success", "Embargo aplicado");
		System.out.println("Boton SI Aplicar:" + boton);
		System.out.println("Msj Pasarela:" + mensajePasarela);
		System.out.println("Num proceso:" + embargo.getNumProceso());
		System.out.println("Fecha Oficio:" + embargo.getFechaOficio());
		System.out.println("Tipo Embargo:" + embargo.getTipoEmbargo());
		System.out.println("Estado: " + embargo.getEmbargoProcesado());
		/*
		System.out.println("Id embargo:" + embargo.getIdEmbargo());
		System.out.println("Id Autoridad:" + embargo.getIdAutoridad());
		System.out.println("Num proceso:" + embargo.getNumProceso());
		System.out.println("Fecha Oficio:" + embargo.getFechaOficio());
		System.out.println("Tipo Embargo:" + embargo.getTipoEmbargo());
		System.out.println("Num Cuenta Agrario:" + embargo.getNumCuentaAgrario());
		for (Demandado demandado : embargo.getDemandados()) {
			ArrayList<Intento> intentos = new ArrayList<>();
			Intento intento = new Intento(LocalDate.now(), true, mensajePasarela, demandado.getCuentas());
			intentos.add(intento);
			demandado.setIntentos(intentos);
		}
		model.addAttribute("titulo", "App");
		model.addAttribute("form", "Formulario");
		model.addAttribute("id", embargo.getIdAutoridad());
		embargo.setEmbargoProcesado(true);
		authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();
		embargo.setIdAutoridad(username);*/
		// EmbargosController.guardarEmbargo(embargo);
		return "autoridad/coactivo/gestor/msj";
	}

	@Secured("ROLE_COACTIVO")
	@RequestMapping(value = "/gestor/aplicar/{boton}/{mensajePasarela}", method = RequestMethod.POST, params = "action=noaplicar")
	public String noAplicarMedida(@ModelAttribute(name = "embargo") EmbargoCoactivo embargo,
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
		authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();
		embargo.setIdAutoridad(username);
		// EmbargosController.guardarEmbargo(embargo);
		return "redirect:/autoridad/coactivo/gestor";
	}

	@Secured("ROLE_COACTIVO")
	@RequestMapping(value = "/gestor/form", method = RequestMethod.POST, params = "action=consultar")
	public String consultar(@ModelAttribute(name = "embargo") EmbargoCoactivo embargo, Model model,
			RedirectAttributes flash) throws JSONException {
		/*
		 * Consulta selector = new Consulta(); if (!consulta(embargo).isEmpty()) {
		 * 
		 * selector.setSelector(consulta(embargo)); Gson gson = new Gson(); String
		 * consulta = gson.toJson(selector); System.out.println("Consulta: " +
		 * consulta); String mensaje = EmbargosController.consultaGeneral(consulta);
		 * System.out.println("Mensaje: " + mensaje); ArrayList<EmbargoCoactivo>
		 * embargos = new ArrayList<EmbargoCoactivo>(); mensaje = "[" + mensaje + "]";
		 * JSONArray myjson = new JSONArray(mensaje); for (int i = 0; i <
		 * myjson.length(); i++) { JSONObject jsonRecord =
		 * myjson.getJSONObject(i).getJSONObject("Record");
		 * embargos.add(jsontoObject(jsonRecord)); }
		 * 
		 * model.addAttribute("titulo", "Consulta"); model.addAttribute("form",
		 * "Consultas"); model.addAttribute("id", embargo.getIdAutoridad());
		 * model.addAttribute("embargos", embargos);
		 * 
		 * } else { flash.addFlashAttribute("warning",
		 * "No se puede Consultar, Por favor ingresar el campo a consultar"); return
		 * "redirect:/autoridad/coactivo/gestor"; } return
		 * "autoridad/coactivo/gestor/consulta";
		 */
		ArrayList<EmbargoCoactivo> embargos = new ArrayList<EmbargoCoactivo>();
		for (int i = 0; i < 2; i++) {
			EmbargoCoactivo prueba = (EmbargoCoactivo) SimulacionCasos.generarEmbargoDian();
			embargos.add(prueba);
		}
		model.addAttribute("titulo", "Consulta");
		model.addAttribute("form", "Consultas");
		model.addAttribute("embargos", embargos);
		model.addAttribute("boton", "consulta");
		return "autoridad/coactivo/gestor/consulta";
	}
	
	@PostMapping("/gestor/msj/{boton}")
	public String inmsj(@ModelAttribute(name = "embargo") EmbargoCoactivo embargo, RedirectAttributes flash,
			@PathVariable(value = "boton") String boton) {
		flash.addFlashAttribute("embargo", embargo);
		flash.addFlashAttribute("boton", boton);
		flash.addFlashAttribute("success", "Embargo aplicado");
		if(boton.equals("all")) {
			return "redirect:/autoridad/coactivo/gestor/main";
		}else {
			return "redirect:/autoridad/coactivo/gestor/consulta";
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
		return "autoridad/coactivo/gestor/main";
	}
	
	@GetMapping("/gestor/consulta")
	public String outconsulta(Model model,@ModelAttribute(name = "embargo") EmbargoCoactivo embargo) {
		System.out.println("Id Autoridad:" + embargo.getIdAutoridad());
		System.out.println("Num proceso:" + embargo.getNumProceso());
		System.out.println("Fecha Oficio:" + embargo.getFechaOficio());
		System.out.println("Tipo Embargo:" + embargo.getTipoEmbargo());
		System.out.println("Num Cuenta Agrario:" + embargo.getNumCuentaAgrario());
		ArrayList<EmbargoCoactivo> embargos = new ArrayList<>();
		EmbargoCoactivo prueba = (EmbargoCoactivo) SimulacionCasos.generarEmbargoDian();
		embargos.add(prueba);
		embargo.setEmbargoProcesado(true);
		embargos.add(embargo);
		model.addAttribute("titulo", "App");
		model.addAttribute("form", "Formulario");
		model.addAttribute("embargos", embargos);
		return "autoridad/coactivo/gestor/consulta";
	}
	
	
	public HashMap<String, String> consulta(EmbargoCoactivo embargo) {
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
						if (!embargo.getNumCuentaAgrario().isEmpty()) {
							campos.put("numCuentaAgrario", embargo.getNumCuentaAgrario());
						} else {

							if (!embargo.getDemandados().get(0).getIdentificacion().isEmpty()) {
								campos.put("identificacion", embargo.getDemandados().get(0).getIdentificacion());
							} else {
								if (embargo.getDemandados().get(0).getTipoIdentificacion() != null) {
									campos.put("tipoIdentificacion",
											embargo.getDemandados().get(0).getTipoIdentificacion().toString());
								} else {
									if (!embargo.getDemandados().get(0).getNombres().isEmpty()) {
										campos.put("nombres", embargo.getDemandados().get(0).getNombres());
									} else {
										if (!embargo.getDemandados().get(0).getApellidos().isEmpty()) {
											campos.put("apellidos", embargo.getDemandados().get(0).getApellidos());
										} else {
											if (!embargo.getDemandados().get(0).getResEmbargo().isEmpty()) {
												campos.put("resEmbargo",
														embargo.getDemandados().get(0).getResEmbargo());
											} else {
												if (embargo.getDemandados().get(0).getFechaResolucion() != null) {
													campos.put("fechaResolucion", embargo.getDemandados().get(0)
															.getFechaResolucion().toString());
												} else {
													if (embargo.getDemandados().get(0).getMontoAEmbargar() != null) {
														campos.put("montoAEmbargar", embargo.getDemandados().get(0)
																.getMontoAEmbargar().toString());
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

	public EmbargoCoactivo jsontoObject(JSONObject jsonRecord) throws JSONException {
		EmbargoCoactivo embargoCoactivo = new EmbargoCoactivo();

		if (jsonRecord.has("numProceso")) {
			embargoCoactivo.setNumProceso(jsonRecord.getString("numProceso"));
		}
		if (jsonRecord.has("numOficio")) {
			embargoCoactivo.setNumOficio(jsonRecord.getString("numOficio"));
		}
		if (jsonRecord.has("fechaOficio")) {
			JSONObject jsonFecha = jsonRecord.getJSONObject("fechaOficio");
			LocalDate localDate = LocalDate.of(Integer.parseInt(jsonFecha.getString("year")),
					Integer.parseInt(jsonFecha.getString("month")), Integer.parseInt(jsonFecha.getString("day")));
			embargoCoactivo.setFechaOficio(localDate);
		}
		if (jsonRecord.has("tipoEmbargo")) {
			embargoCoactivo.setTipoEmbargo(TipoEmbargo.valueOf(jsonRecord.getString("tipoEmbargo")));
		}
		if (jsonRecord.has("numCuentaAgrario")) {
			embargoCoactivo.setNumCuentaAgrario(jsonRecord.getString("numCuentaAgrario"));
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
				// demandado.setResEmbargo(jsonDemandados.getJSONObject(k).getString("resEmbargo"));
				// JSONObject jsonFecha =
				// jsonDemandados.getJSONObject(k).getJSONObject("fechaResolucion");
				// LocalDate localDate =
				// LocalDate.of(Integer.parseInt(jsonFecha.getString("year")),
				// Integer.parseInt(jsonFecha.getString("month")),
				// Integer.parseInt(jsonFecha.getString("day")));
				// demandado.setFechaResolucion(localDate);
				demandado
						.setMontoAEmbargar(new BigDecimal(jsonDemandados.getJSONObject(k).getString("montoAEmbargar")));
				demandados.add(demandado);
			}
			embargoCoactivo.setDemandados(demandados);
		}
		return embargoCoactivo;
	}

	/*
	 * System.out.println("Hola Mundo");
	 * System.out.println(embargoCoactivo.getDemandadosDian() != null ?
	 * embargoCoactivo.getDemandadosDian().size() : "null list"); for(DemandadoDian
	 * demandadoDian: embargoCoactivo.getDemandadosDian()) {
	 * System.out.println("Nombre demandante: "+demandadoDian.getResEmbargo()); }
	 */

}
