package unicauca.front.end.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.GrayColor;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

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
		EmbargoCoactivo embargoCoactivo = new EmbargoCoactivo();
		embargoCoactivo.getDemandados().add(new Demandado());
		model.addAttribute("titulo", "App");
		model.addAttribute("form", "Formulario");
		model.addAttribute("embargoCoactivo", embargoCoactivo);

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
	 * @Secured("ROLE_COACTIVO")
	 * 
	 * @GetMapping("/gestor/cargar") public String cargarEmbargo(Model model) {
	 * embargo = (EmbargoCoactivo) SimulacionCasos.generarEmbargoDian();
	 * model.addAttribute("id", embargo.getIdAutoridad());
	 * model.addAttribute("titulo", "Embargo"); model.addAttribute("form",
	 * "Formulario"); model.addAttribute("embargo", embargo);
	 * model.addAttribute("boton", "all"); return "autoridad/coactivo/gestor/main";
	 * }
	 */

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

	public EmbargoCoactivo asignarEmbargo(EmbargoCoactivo embargoCoactivo, Demandado demandado, Row row, int i) {
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
		 * System.out.println("Id embargo:" + embargo.getIdEmbargo());
		 * System.out.println("Id Autoridad:" + embargo.getIdAutoridad());
		 * System.out.println("Num proceso:" + embargo.getNumProceso());
		 * System.out.println("Fecha Oficio:" + embargo.getFechaOficio());
		 * System.out.println("Tipo Embargo:" + embargo.getTipoEmbargo());
		 * System.out.println("Num Cuenta Agrario:" + embargo.getNumCuentaAgrario());
		 * for (Demandado demandado : embargo.getDemandados()) { ArrayList<Intento>
		 * intentos = new ArrayList<>(); Intento intento = new Intento(LocalDate.now(),
		 * true, mensajePasarela, demandado.getCuentas()); intentos.add(intento);
		 * demandado.setIntentos(intentos); } model.addAttribute("titulo", "App");
		 * model.addAttribute("form", "Formulario"); model.addAttribute("id",
		 * embargo.getIdAutoridad()); embargo.setEmbargoProcesado(true); authentication
		 * = SecurityContextHolder.getContext().getAuthentication(); String username =
		 * authentication.getName(); embargo.setIdAutoridad(username);
		 */
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
		embargo.setEmbargoProcesado(false);
		// authentication = SecurityContextHolder.getContext().getAuthentication();
		// String username = authentication.getName();
		// embargo.setIdAutoridad(username);
		// EmbargosController.guardarEmbargo(embargo);
		return "redirect:/autoridad/coactivo/gestor";
	}

	@Secured("ROLE_COACTIVO")
	@RequestMapping(value = "/gestor/form", method = RequestMethod.POST, params = "action=consultar")
	public String consultar(@ModelAttribute(name = "embargo") EmbargoCoactivo embargo, Model model,
			RedirectAttributes flash) throws JSONException {

		Consulta selector = new Consulta();
		if (!consulta(embargo).isEmpty()) {
			selector.setSelector(consulta(embargo));
			Gson gson = new Gson();
			String consulta = gson.toJson(selector);
			System.out.println("Consulta: " + consulta);
			//String mensaje = EmbargosController.consultaGeneral(consulta);
			String mensaje="{\"key\":1,\"Record\":{\"idAutoridad\":\"AUT1\",\"numProceso\":\"PRC1\",\"fechaOficio\":{\"day\":23,\"month\":3,\"year\":2017},\"tipoEmbargo\":\"COACTIVO\",\"numCuentaAgrario\":92345654,\"demandados\":[{\"identificacion\":789,\"nombres\":\"SANTIAGO\",\"apellidos\":\"ORTEGA\",\"tipoIdentificacion\":\"NATURAL\",\"resEmbargo\":\"RES178\",\"fechaResolucion\":{\"day\":18,\"month\":5,\"year\":2018},\"montoAEmbargar\":34500000},{\"identificacion\":678,\"nombres\":\"CARLOS\",\"apellidos\":\"RUIZ\",\"tipoIdentificacion\":\"NATURAL\",\"resEmbargo\":\"RES478\",\"fechaResolucion\":{\"day\":10,\"month\":4,\"year\":2019},\"montoAEmbargar\":14800000}]}},{\"key\":2,\"Record\":{\"idAutoridad\":\"AUT2\",\"numProceso\":\"PRC2\",\"numOficio\":\"OFC2\",\"fechaOficio\":{\"day\":24,\"month\":8,\"year\":2018},\"tipoEmbargo\":\"JUDICIAL\",\"montoAEmbargar\":35600000,\"numCuentaAgrario\":92567432,\"demandados\":[{\"identificacion\":543,\"nombres\":\"JUAN\",\"apellidos\":\"RUIZ\",\"tipoIdentificacion\":\"NATURAL\",\"resEmbargo\":\"RES348\",\"fechaResolucion\":{\"day\":23,\"month\":11,\"year\":2017},\"montoAEmbargar\":14500000},{\"identificacion\":212,\"nombres\":\"DIEGO\",\"apellidos\":\"LOPEZ\",\"tipoIdentificacion\":\"NATURAL\",\"resEmbargo\":\"RES328\",\"fechaResolucion\":{\"day\":28,\"month\":10,\"year\":2018},\"montoAEmbargar\":24500000}]}}";
			System.out.println("Mensaje: " + mensaje);
			ArrayList<EmbargoCoactivo> embargos = new ArrayList<EmbargoCoactivo>();
			mensaje = "[" + mensaje + "]";
			JSONArray myjson = new JSONArray(mensaje);

			for (int i = 0; i < myjson.length(); i++) {
				JSONObject jsonRecord = myjson.getJSONObject(i).getJSONObject("Record");
				embargos.add(jsontoObject(jsonRecord));
			}
			
			for (int i = 0; i < embargos.size(); i++) {
				embargos.get(1).setEmbargoProcesado(true);
			}

			model.addAttribute("titulo", "Consulta");
			model.addAttribute("form", "Consultas");
			model.addAttribute("embargos", embargos);
			model.addAttribute("boton", "consulta");
			
		} else {
			flash.addFlashAttribute("warning", "No se puede Consultar, Por favor ingresar el campo a consultar");
			return "redirect:/autoridad/coactivo/gestor";
		}
		return "autoridad/coactivo/gestor/consulta";

		/*
		 * ArrayList<EmbargoCoactivo> embargos = new ArrayList<EmbargoCoactivo>(); for
		 * (int i = 0; i < 2; i++) { EmbargoCoactivo prueba = (EmbargoCoactivo)
		 * SimulacionCasos.generarEmbargoDian(); embargos.add(prueba); }
		 * model.addAttribute("titulo", "Consulta");
		 *  model.addAttribute("form",
		 * "Consultas"); 
		 * model.addAttribute("embargos", embargos);
		 * model.addAttribute("boton", "consulta"); return
		 * "autoridad/coactivo/gestor/consulta";
		 */
	}

	@PostMapping("/gestor/msj/{boton}")
	public String inmsj(@ModelAttribute(name = "embargo") EmbargoCoactivo embargo, RedirectAttributes flash,
			@PathVariable(value = "boton") String boton) {

		System.out.println("Num proceso:" + embargo.getNumProceso());
		System.out.println("Fecha Oficio:" + embargo.getFechaOficio());
		System.out.println("Tipo Embargo:" + embargo.getTipoEmbargo());
		System.out.println("Num Cuenta Agrario:" + embargo.getNumCuentaAgrario());
		for (Demandado demandado : embargo.getDemandados()) {
			System.out.println("Id Demandado:" + demandado.getIdentificacion());
			System.out.println("Nombres demandado:" + demandado.getNombres());
			System.out.println("Fecha res:" + demandado.getFechaResolucion());
		}
		flash.addFlashAttribute("embargo", embargo);
		flash.addFlashAttribute("boton", boton);
		flash.addFlashAttribute("success", "Embargo aplicado");
		if (boton.equals("all")) {
			return "redirect:/autoridad/coactivo/gestor/main";
		} else {
			return "redirect:/autoridad/coactivo/gestor/consulta";
		}
	}

	@GetMapping("/gestor/main")
	public String outmsj(@ModelAttribute(name = "embargo") EmbargoCoactivo embargo, Model model) {

		model.addAttribute("titulo", "App");
		model.addAttribute("form", "Formulario");
		return "autoridad/coactivo/gestor/main";
	}

	@GetMapping("/gestor/consulta")
	public String outconsulta(Model model, @ModelAttribute(name = "embargo") EmbargoCoactivo embargo) {
		// System.out.println("Id Autoridad:" + embargo.getIdAutoridad());
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

	@GetMapping("/imprimir")
	public ResponseEntity<byte[]> print(Model model) throws DocumentException, IOException {
		authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();
		// Buscar todos los embargos registrados por el username

		String filepdf = "file.pdf";
		ArrayList<EmbargoCoactivo> embargos = getAllEmbargos();

		createPdf(filepdf, embargos);

		HttpHeaders headers = new HttpHeaders();
		Path pdfPath = Paths.get(filepdf);
		byte[] pdf = Files.readAllBytes(pdfPath);
		headers.setContentType(MediaType.parseMediaType("application/pdf"));
		headers.add("Content-Disposition", "inline; filename=" + filepdf);
		headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
		ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(pdf, headers, HttpStatus.OK);

		return response;

	}

	public void createPdf(String dest, ArrayList<EmbargoCoactivo> embargos)
			throws FileNotFoundException, DocumentException {
		Document document = new Document();
		PdfWriter.getInstance(document, new FileOutputStream(dest));
		document.open();

		for (int i = 0; i < embargos.size(); i++) {

			PdfPTable table = new PdfPTable(2);
			PdfPTable table2 = new PdfPTable(7);
			table.setSpacingBefore(10f);
			table.setSpacingAfter(12.5f);
			table2.setSpacingBefore(10f);
			table2.setSpacingAfter(12.5f);
			table.setWidthPercentage(100);
			table.setHorizontalAlignment(Element.ALIGN_CENTER);

			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
			table.addCell("Numero Proceso: " + embargos.get(i).getNumProceso());
			table.addCell("Numero Oficio: " + embargos.get(i).getNumOficio());
			table.addCell("Fecha Oficio: " + embargos.get(i).getFechaOficio());
			table.addCell("Tipo Embargo: " + embargos.get(i).getTipoEmbargo());
			table.addCell("Numero Cuenta Banco Agrario: " + embargos.get(i).getNumCuentaAgrario());

			document.add(table);

			Font f = new Font(FontFamily.HELVETICA, 13, Font.NORMAL, GrayColor.GRAYWHITE);
			PdfPCell cell = new PdfPCell(new Phrase("Demandantes", f));
			cell.setBackgroundColor(GrayColor.GRAYBLACK);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setColspan(7);
			table2.setWidthPercentage(100);
			table2.addCell(cell);
			table2.getDefaultCell().setBackgroundColor(new GrayColor(0.75f));
			for (int j = 0; j < 1; j++) {
				table2.addCell("Identificacion");
				table2.addCell("Tipo Identificacion");
				table2.addCell("Nombres");
				table2.addCell("Apellidos");
				table2.addCell("Resolucion Embargo");
				table2.addCell("Fecha Resolucion");
				table2.addCell("Monto a Embargar");
			}
			table2.setHeaderRows(1);
			table2.getDefaultCell().setBackgroundColor(GrayColor.GRAYWHITE);
			table2.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
			for (Demandado demandado : embargos.get(i).getDemandados()) {
				table2.addCell(demandado.getIdentificacion());
				table2.addCell(demandado.getTipoIdentificacion().toString());
				table2.addCell(demandado.getNombres());
				table2.addCell(demandado.getApellidos());
				table2.addCell(demandado.getResEmbargo());
				table2.addCell(demandado.getFechaResolucion().toString());
				table2.addCell(demandado.getMontoAEmbargar().toString());
			}
			document.add(table2);
			document.newPage();
		}

		document.close();
	}

	public ArrayList<EmbargoCoactivo> getAllEmbargos() {
		ArrayList<EmbargoCoactivo> embargos = new ArrayList<EmbargoCoactivo>();
		for (int i = 0; i < 2; i++) {
			EmbargoCoactivo prueba = (EmbargoCoactivo) SimulacionCasos.generarEmbargoDian();
			embargos.add(prueba);
		}
		return embargos;
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
				demandado.setResEmbargo(jsonDemandados.getJSONObject(k).getString("resEmbargo"));
				JSONObject jsonFecha =jsonDemandados.getJSONObject(k).getJSONObject("fechaResolucion");
				LocalDate localDate =LocalDate.of(Integer.parseInt(jsonFecha.getString("year")),
				Integer.parseInt(jsonFecha.getString("month")),
				Integer.parseInt(jsonFecha.getString("day")));
				demandado.setFechaResolucion(localDate);
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
