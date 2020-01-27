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

import org.apache.poi.ss.usermodel.*;

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
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.GrayColor;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import controladores.EmbargosController;
import enumeraciones.TipoEmbargo;
import enumeraciones.TipoIdentificacion;
import modelo.Demandado;
import modelo.Demandante;
import modelo.EmbargoJudicial;
import modelo.Intento;
import modelo.Usuario;
//import simulacion.SimulacionCasos;
import simulacion.SimulacionPasarelas;
import unicauca.front.end.service.Consulta;
import unicauca.front.end.service.Service;
import util.SessionHelper;

@Controller
@RequestMapping("/autoridad/judicial")
public class JudicialController {

	private SimulacionPasarelas simulacionPasarela;
	private SessionHelper session;
	private Service service;

	public JudicialController() {
		simulacionPasarela = new SimulacionPasarelas();
		session = new SessionHelper();
		service = new Service();
	}

	// ------------GESTOR-----------
	@Secured("ROLE_JUDICIAL")
	@GetMapping("/gestor")
	public String crearEmbargo(Model model) {

		EmbargoJudicial embargo = new EmbargoJudicial();
		embargo.getDemandantes().add(new Demandante());
		embargo.getDemandados().add(new Demandado());
		model.addAttribute("titulo", "App");
		model.addAttribute("form", "Formulario");
		model.addAttribute("embargo", embargo);
		model.addAttribute("boton", "all");
		return "autoridad/judicial/gestor/main";
	}

	@RequestMapping(value = "/gestor/form", method = RequestMethod.POST, params = "action=cargar")
	public String cargarEmbargo(@RequestParam("file") MultipartFile archivo, Model model, RedirectAttributes flash)
			throws IOException {
		boolean band = false;
		EmbargoJudicial embargoJudicial = new EmbargoJudicial();
		if (!archivo.isEmpty()) {
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
	public String addDemandante(@ModelAttribute(name = "embargo") EmbargoJudicial embargo, Model model,
			RedirectAttributes flash) {
		int tamDemandantes = embargo.getDemandantes().size();
		if (isDemandante(embargo)) {
			if (findDemandante(embargo.getDemandantes().get(tamDemandantes - 1).getIdentificacion(),
					embargo.getDemandantes()) == 1) {
				embargo.getDemandantes().add(new Demandante());
			} else {
				flash.addFlashAttribute("warning", "La identificacion del Demandante esta repetida");
			}
		} else {
			flash.addFlashAttribute("warning", "Por favor Llenar el formulario del Demandante");
		}
		flash.addFlashAttribute("embargo", embargo);
		flash.addFlashAttribute("boton", "all");
		return "redirect:/autoridad/judicial/gestor/main";
	}

	@RequestMapping(value = "/gestor/form", method = RequestMethod.POST, params = "action=demandado")
	public String addDemandado(@ModelAttribute(name = "embargo") EmbargoJudicial embargo, Model model,
			RedirectAttributes flash) {
		int tamDemandados = embargo.getDemandados().size();
		if (isDemandado(embargo)) {
			if (findDemandado(embargo.getDemandados().get(tamDemandados - 1).getIdentificacion(),
					embargo.getDemandados()) == 1) {
				embargo.getDemandados().add(new Demandado());
			} else {
				flash.addFlashAttribute("warning", "La identificacion del Demandado esta repetida");
			}
		} else {
			flash.addFlashAttribute("warning", "Por favor Llenar el formulario del Demandado");
		}
		flash.addFlashAttribute("embargo", embargo);
		flash.addFlashAttribute("boton", "all");
		return "redirect:/autoridad/judicial/gestor/main";
	}

	@RequestMapping(value = "/gestor/form", method = RequestMethod.POST, params = "action=aplicar")
	public String aplicar(@ModelAttribute(name = "embargo") EmbargoJudicial embargo, Model model,
			RedirectAttributes flash) {
		boolean band = false;
		int tamDemandantes = embargo.getDemandantes().size();
		int tamDemandados = embargo.getDemandados().size();
		if (isValid(embargo)) {
			if (EmbargosController.obtenerEmbargo(embargo.getNumProceso()).isEmpty()) {
				if (findDemandante(embargo.getDemandantes().get(tamDemandantes - 1).getIdentificacion(),
						embargo.getDemandantes()) == 1) {
					if (findDemandado(embargo.getDemandados().get(tamDemandados - 1).getIdentificacion(),
							embargo.getDemandados()) == 1) {
						try {
							KieSession sessionStatefull = session.obtenerSesion();
							String mensajePasarela = simulacionPasarela.llamarPasarelas(embargo.getDemandados());
							sessionStatefull.insert(embargo);
							sessionStatefull.fireAllRules();
							session.cerrarSesion(sessionStatefull);
							model.addAttribute("titulo", "Aplicar");
							model.addAttribute("form", "Resultado");
							model.addAttribute("embargo", embargo);
							model.addAttribute("mensajePasarela", mensajePasarela);
							model.addAttribute("mensaje", service.imprimir(embargo));
							model.addAttribute("boton", "all");
							band = true;

						} catch (NullPointerException e) {
							flash.addFlashAttribute("warning", "No se puede Aplicar");
						}
					} else {
						flash.addFlashAttribute("warning", "La identificacion del Demandado esta repetida");
					}
				} else {
					flash.addFlashAttribute("warning", "La identificacion del Demandante esta repetida");
				}
			} else {
				flash.addFlashAttribute("warning", "No se puede Aplicar,El embargo ya existe");
				embargo.setNumProceso(null);
			}
		} else {
			flash.addFlashAttribute("warning", "No se puede Aplicar,Por favor Llenar el formulario completo");
		}
		if (band == true) {
			return "autoridad/judicial/gestor/output";
		} else {
			flash.addFlashAttribute("boton", "all");
			flash.addFlashAttribute("embargo", embargo);
			return "redirect:/autoridad/judicial/gestor/main";
		}

	}

	@RequestMapping(value = "/gestor/data/{consulta}", method = RequestMethod.POST, params = "action=aplicar")
	public String reaplicar(@ModelAttribute(name = "embargo") EmbargoJudicial embargo, Model model,
			RedirectAttributes flash, @PathVariable(value = "consulta") String consulta) {

		EmbargoJudicial embargofind = BackEndController.obtenerEmbargoJudicial(embargo.getNumProceso());
		try {
			KieSession sessionStatefull = session.obtenerSesion();
			String mensajePasarela = simulacionPasarela.llamarPasarelas(embargofind.getDemandados());
			sessionStatefull.insert(embargofind);
			sessionStatefull.fireAllRules();
			session.cerrarSesion(sessionStatefull);

			model.addAttribute("titulo", "Aplicar");
			model.addAttribute("form", "Resultado");
			model.addAttribute("embargo", embargofind);
			model.addAttribute("mensajePasarela", mensajePasarela);
			model.addAttribute("mensaje", service.imprimir(embargofind));
			model.addAttribute("boton", "consulta");
			model.addAttribute("consulta", consulta);
			return "autoridad/judicial/gestor/output";
		} catch (NullPointerException e) {
			flash.addFlashAttribute("warning", "No se puede Aplicar,Por favor llenar el formulario");
			return "redirect:/autoridad/judicial/gestor";
		}
	}

	@RequestMapping(value = "/gestor/data/{consulta}", method = RequestMethod.POST, params = "action=desembargar")
	public String desembargar(@ModelAttribute(name = "embargo") EmbargoJudicial embargo, RedirectAttributes flash,
			@PathVariable(value = "consulta") String consulta) {
		EmbargoJudicial embargofind = BackEndController.obtenerEmbargoJudicial(embargo.getNumProceso());
		embargofind.setEmbargado(false);
		EmbargosController.editarEmbargo(embargofind);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		flash.addFlashAttribute("success", "El Embargo ha sido desembargado");
		flash.addFlashAttribute("boton", "consulta");
		flash.addAttribute("consulta", consulta);
		return "redirect:/autoridad/judicial/gestor/consulta";

	}

	@RequestMapping(value = "/gestor/aplicar/{boton}/{mensajePasarela}/{consulta}", method = RequestMethod.POST, params = "action=siaplicar")
	public String aplicarMedida(@ModelAttribute(name = "embargo") EmbargoJudicial embargo, Model model,
			@PathVariable(value = "mensajePasarela") String mensajePasarela,
			@PathVariable(value = "boton") String boton, @PathVariable(value = "consulta") String consulta) {

		for (int i = 0; i < embargo.getDemandados().size(); i++) {
			ArrayList<Intento> intentos = new ArrayList<>();
			Intento intento = new Intento(LocalDate.now(), true, mensajePasarela,
					embargo.getDemandados().get(i).getCuentas());
			intentos.add(intento);
			embargo.getDemandados().get(i).setIntentos(intentos);
		}
		model.addAttribute("titulo", "App");
		model.addAttribute("form", "Formulario");
		model.addAttribute("embargo", embargo);
		model.addAttribute("boton", boton);
		model.addAttribute("consulta", consulta);
		embargo.setEmbargoProcesado(true);
		embargo.setEmbargado(true);

		if (boton.equals("all")) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String username = authentication.getName();
			Usuario usuarioLogin = BackEndController.obtenerUsuario(username);
			embargo.setIdAutoridad(usuarioLogin.getIdAutoridad());
			embargo.setUsername(username);
			EmbargosController.guardarEmbargo(embargo);
		} else {
			EmbargosController.editarEmbargo(embargo);
		}
		return "autoridad/judicial/gestor/msj";
	}

	@RequestMapping(value = "/gestor/aplicar/{boton}/{mensajePasarela}/{consulta}", method = RequestMethod.POST, params = "action=noaplicar")
	public String noAplicarMedida(@ModelAttribute(name = "embargo") EmbargoJudicial embargo, Model model,
			@PathVariable(value = "mensajePasarela") String mensajePasarela, RedirectAttributes flash,
			@PathVariable(value = "boton") String boton, @PathVariable(value = "consulta") String consulta) {

		for (int i = 0; i < embargo.getDemandados().size(); i++) {
			ArrayList<Intento> intentos = new ArrayList<>();
			Intento intento = new Intento(LocalDate.now(), false, mensajePasarela,
					embargo.getDemandados().get(i).getCuentas());
			intentos.add(intento);
			embargo.getDemandados().get(i).setIntentos(intentos);
		}
		embargo.setEmbargoProcesado(false);
		embargo.setEmbargado(false);

		if (boton.equals("all")) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String username = authentication.getName();
			Usuario usuarioLogin = BackEndController.obtenerUsuario(username);
			embargo.setIdAutoridad(usuarioLogin.getIdAutoridad());
			embargo.setUsername(username);
			EmbargosController.guardarEmbargo(embargo);
		} else {
			EmbargosController.editarEmbargo(embargo);
		}

		flash.addFlashAttribute("embargo", embargo);
		flash.addFlashAttribute("boton", boton);
		flash.addFlashAttribute("success", "Embargo NO aplicado");

		return "redirect:/autoridad/judicial/gestor/main";
	}

	@RequestMapping(value = "/gestor/form", method = RequestMethod.POST, params = "action=consultar")
	public String consultar(@ModelAttribute(name = "embargo") EmbargoJudicial embargo, Model model,
			RedirectAttributes flash) throws JSONException {
		
		if (emptyField(embargo)) {
			Gson gson = new Gson();
			String consulta = gson.toJson(consulta(embargo, "gestor"));
			ArrayList<EmbargoJudicial> embargos = onJson(consulta);
			if (!embargos.isEmpty()) {
				model.addAttribute("titulo", "Consulta");
				model.addAttribute("form", "Consultas");
				model.addAttribute("embargos", embargos);
				model.addAttribute("boton", "consulta");
				model.addAttribute("consulta", consulta);
				return "autoridad/judicial/gestor/consulta";
			} else {
				flash.addFlashAttribute("warning", "No se encontraron resultados");
				return "redirect:/autoridad/judicial/gestor";
			}
		} else {
			flash.addFlashAttribute("warning", "No se puede Consultar, Por favor ingresar el campo a consultar");
			return "redirect:/autoridad/judicial/gestor";
		}
	}

	@PostMapping("/gestor/msj/{boton}/{consulta}")
	public String inmsj(@ModelAttribute(name = "embargo") EmbargoJudicial embargo, RedirectAttributes flash,
			@PathVariable(value = "boton") String boton, @PathVariable(value = "consulta") String consulta) {

		flash.addFlashAttribute("embargo", embargo);
		flash.addFlashAttribute("boton", boton);
		flash.addFlashAttribute("success", "Embargo aplicado");

		if (boton.equals("all")) {
			return "redirect:/autoridad/judicial/gestor/main";
		} else {
			flash.addAttribute("consulta", consulta);
			return "redirect:/autoridad/judicial/gestor/consulta";
		}
	}

	@GetMapping("/gestor/main")
	public String outmsj(@ModelAttribute(name = "embargo") EmbargoJudicial embargo, Model model) {
		model.addAttribute("titulo", "App");
		model.addAttribute("form", "Formulario");
		return "autoridad/judicial/gestor/main";
	}

	@GetMapping("/gestor/consulta")
	public String outconsulta(Model model, @ModelAttribute(name = "consulta") String consulta) throws JSONException {
		ArrayList<EmbargoJudicial> embargos = onJson(consulta);

		model.addAttribute("titulo", "App");
		model.addAttribute("form", "Formulario");
		model.addAttribute("embargos", embargos);
		return "autoridad/judicial/gestor/consulta";
	}

	@GetMapping("/secretario")
	public String judicial(Model model) {
		EmbargoJudicial embargoJudicial = new EmbargoJudicial();
		embargoJudicial.getDemandantes().add(new Demandante());
		embargoJudicial.getDemandados().add(new Demandado());
		model.addAttribute("boton", "all");
		model.addAttribute("titulo", "App");
		model.addAttribute("form", "Formulario");
		model.addAttribute("embargoJudicial", embargoJudicial);
		return "autoridad/judicial/secretario/main";
	}

	@RequestMapping(value = "/secretario/form", method = RequestMethod.POST, params = "action=consultar")
	public String judicial(@ModelAttribute(name = "embargoJudicial") EmbargoJudicial embargo, Model model,
			RedirectAttributes flash) throws JSONException {

		if (emptyField(embargo)) {
			Gson gson = new Gson();
			String consulta = gson.toJson(consulta(embargo, "secretario"));
			ArrayList<EmbargoJudicial> embargos = onJson(consulta);
			if (!embargos.isEmpty()) {
				model.addAttribute("titulo", "Consulta");
				model.addAttribute("form", "Consultas");
				model.addAttribute("embargos", embargos);
				model.addAttribute("boton", "consulta");
				model.addAttribute("consulta", consulta);
				return "autoridad/judicial/secretario/consulta";
			} else {
				flash.addFlashAttribute("warning", "No se encontraron resultados");
				return "redirect:/autoridad/judicial/secretario";
			}

		} else {
			flash.addFlashAttribute("warning", "No se puede Consultar, Por favor ingresar el campo a consultar");
			return "redirect:/autoridad/judicial/secretario";
		}

	}

	@GetMapping("/imprimir/{consulta}")
	public ResponseEntity<byte[]> print(@PathVariable(value = "consulta") String consulta)
			throws DocumentException, IOException, JSONException {

		String filepdf = "file.pdf";
		ArrayList<EmbargoJudicial> embargos = onJson(consulta);
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

	public ArrayList<EmbargoJudicial> onJson(String consulta) throws JSONException {
		String consultanew = consulta;
		String mensaje = EmbargosController.consultaGeneral(consultanew);
		ArrayList<EmbargoJudicial> embargos = new ArrayList<EmbargoJudicial>();
		mensaje = "[" + mensaje + "]";
		JSONArray myjson = new JSONArray(mensaje);
		for (int i = 0; i < myjson.length(); i++) {
			JSONObject jsonRecord = myjson.getJSONObject(i).getJSONObject("Record");
			embargos.add(jsontoObject(jsonRecord));
		}
		return embargos;
	}

	public void createPdf(String dest, ArrayList<EmbargoJudicial> embargos)
			throws FileNotFoundException, DocumentException {
		Document document = new Document();
		PdfWriter.getInstance(document, new FileOutputStream(dest));
		document.open();

		for (int i = 0; i < embargos.size(); i++) {

			PdfPTable table = new PdfPTable(2);
			PdfPTable table2 = new PdfPTable(4);
			PdfPTable table3 = new PdfPTable(5);
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
			table.addCell("Monto a Embargar: " + embargos.get(i).getMontoAEmbargar());
			table.addCell("Numero Cuenta Banco Agrario: " + embargos.get(i).getNumCuentaAgrario());

			document.add(table);

			Font f = new Font(FontFamily.HELVETICA, 13, Font.NORMAL, GrayColor.GRAYWHITE);
			PdfPCell cell = new PdfPCell(new Phrase("Demandantes", f));
			cell.setBackgroundColor(GrayColor.GRAYBLACK);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setColspan(4);
			table2.addCell(cell);
			table2.getDefaultCell().setBackgroundColor(new GrayColor(0.75f));
			for (int j = 0; j < 1; j++) {
				table2.addCell("Identificacion");
				table2.addCell("Tipo Identificacion");
				table2.addCell("Nombres");
				table2.addCell("Apellidos");
			}
			table2.setHeaderRows(1);
			table2.getDefaultCell().setBackgroundColor(GrayColor.GRAYWHITE);
			table2.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
			for (Demandante demandante : embargos.get(i).getDemandantes()) {
				table2.addCell(demandante.getIdentificacion());
				table2.addCell(demandante.getTipoIdentificacion().toString());
				table2.addCell(demandante.getNombres());
				table2.addCell(demandante.getApellidos());
			}
			document.add(table2);

			PdfPCell cell2 = new PdfPCell(new Phrase("Demandados", f));
			cell2.setBackgroundColor(GrayColor.GRAYBLACK);
			cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell2.setColspan(5);
			table3.addCell(cell2);
			table3.getDefaultCell().setBackgroundColor(new GrayColor(0.75f));
			for (int j = 0; j < 1; j++) {
				table3.addCell("Identificacion");
				table3.addCell("Tipo Identificacion");
				table3.addCell("Nombres");
				table3.addCell("Apellidos");
				table3.addCell("Monto a Embargar");
			}
			table3.setHeaderRows(1);
			table3.getDefaultCell().setBackgroundColor(GrayColor.GRAYWHITE);
			table3.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
			for (Demandado demandado : embargos.get(i).getDemandados()) {
				table3.addCell(demandado.getIdentificacion());
				table3.addCell(demandado.getTipoIdentificacion().toString());
				table3.addCell(demandado.getNombres());
				table3.addCell(demandado.getApellidos());
				table3.addCell(demandado.getMontoAEmbargar().toString());
			}
			document.add(table3);
			document.newPage();
		}

		document.close();
	}
	
	public boolean emptyField(EmbargoJudicial embargoJudicial) {
		int tamDemandantes = embargoJudicial.getDemandantes().size();
		int tamDemandados = embargoJudicial.getDemandados().size();
		return !embargoJudicial.getNumProceso().isEmpty() || !embargoJudicial.getNumOficio().isEmpty()
				|| embargoJudicial.getFechaOficio() != null || embargoJudicial.getTipoEmbargo() != null
				|| embargoJudicial.getMontoAEmbargar() != null || !embargoJudicial.getNumCuentaAgrario().isEmpty()
				|| !embargoJudicial.getDemandantes().get(tamDemandantes - 1).getIdentificacion().isEmpty()
				|| embargoJudicial.getDemandantes().get(tamDemandantes - 1).getTipoIdentificacion() != null
				|| !embargoJudicial.getDemandantes().get(tamDemandantes - 1).getNombres().isEmpty()
				|| !embargoJudicial.getDemandantes().get(tamDemandantes - 1).getApellidos().isEmpty()
				|| !embargoJudicial.getDemandados().get(tamDemandados - 1).getIdentificacion().isEmpty()
				|| embargoJudicial.getDemandados().get(tamDemandados - 1).getTipoIdentificacion() != null
				|| !embargoJudicial.getDemandados().get(tamDemandados - 1).getNombres().isEmpty()
				|| !embargoJudicial.getDemandados().get(tamDemandados - 1).getApellidos().isEmpty()
				|| embargoJudicial.getDemandados().get(tamDemandados - 1).getMontoAEmbargar() != null;
	}

	public int findDemandante(String id, ArrayList<Demandante> demandantes) {
		int cont = 0;
		for (Demandante demandante : demandantes) {
			if (demandante.getIdentificacion().equals(id)) {
				cont++;
			}
		}
		return cont;
	}

	public int findDemandado(String id, ArrayList<Demandado> demandados) {
		int cont = 0;
		for (Demandado demandado : demandados) {
			if (demandado.getIdentificacion().equals(id)) {
				cont++;
			}
		}
		return cont;
	}

	public boolean isDemandante(EmbargoJudicial embargoJudicial) {
		int tamDemandantes = embargoJudicial.getDemandantes().size();
		return !embargoJudicial.getDemandantes().get(tamDemandantes - 1).getIdentificacion().isEmpty()
				&& embargoJudicial.getDemandantes().get(tamDemandantes - 1).getTipoIdentificacion() != null
				&& !embargoJudicial.getDemandantes().get(tamDemandantes - 1).getNombres().isEmpty()
				&& !embargoJudicial.getDemandantes().get(tamDemandantes - 1).getApellidos().isEmpty();
	}

	public boolean isDemandado(EmbargoJudicial embargoJudicial) {
		int tamDemandados = embargoJudicial.getDemandados().size();
		return !embargoJudicial.getDemandados().get(tamDemandados - 1).getIdentificacion().isEmpty()
				&& embargoJudicial.getDemandados().get(tamDemandados - 1).getTipoIdentificacion() != null
				&& !embargoJudicial.getDemandados().get(tamDemandados - 1).getNombres().isEmpty()
				&& !embargoJudicial.getDemandados().get(tamDemandados - 1).getApellidos().isEmpty()
				&& embargoJudicial.getDemandados().get(tamDemandados - 1).getMontoAEmbargar() != null;
	}

	public boolean isValid(EmbargoJudicial embargoJudicial) {
		int tamDemandantes = embargoJudicial.getDemandantes().size();

		int tamDemandados = embargoJudicial.getDemandados().size();
		return !embargoJudicial.getNumProceso().isEmpty() && !embargoJudicial.getNumOficio().isEmpty()
				&& embargoJudicial.getFechaOficio() != null && embargoJudicial.getTipoEmbargo() != null
				&& embargoJudicial.getMontoAEmbargar() != null && !embargoJudicial.getNumCuentaAgrario().isEmpty()
				&& !embargoJudicial.getDemandantes().get(tamDemandantes - 1).getIdentificacion().isEmpty()
				&& embargoJudicial.getDemandantes().get(tamDemandantes - 1).getTipoIdentificacion() != null
				&& !embargoJudicial.getDemandantes().get(tamDemandantes - 1).getNombres().isEmpty()
				&& !embargoJudicial.getDemandantes().get(tamDemandantes - 1).getApellidos().isEmpty()
				&& !embargoJudicial.getDemandados().get(tamDemandados - 1).getIdentificacion().isEmpty()
				&& embargoJudicial.getDemandados().get(tamDemandados - 1).getTipoIdentificacion() != null
				&& !embargoJudicial.getDemandados().get(tamDemandados - 1).getNombres().isEmpty()
				&& !embargoJudicial.getDemandados().get(tamDemandados - 1).getApellidos().isEmpty()
				&& embargoJudicial.getDemandados().get(tamDemandados - 1).getMontoAEmbargar() != null;
	}

	public Consulta consulta(EmbargoJudicial embargo, String band) {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();
		Usuario usuarioLogin = BackEndController.obtenerUsuario(username);
		int tamDemandantes = embargo.getDemandantes().size();
		int tamDemandados = embargo.getDemandados().size();
		Consulta consulta = new Consulta();

		if (!embargo.getNumProceso().isEmpty()) {
			consulta.searchNormal("numProceso", embargo.getNumProceso());
		}
		if (!embargo.getNumOficio().isEmpty()) {
			consulta.searchNormal("numOficio", embargo.getNumOficio());
		}
		if (embargo.getFechaOficio() != null) {
			consulta.searchNormal("fechaOficio", embargo.getFechaOficio().toString());
		}
		if (embargo.getTipoEmbargo() != null) {
			consulta.searchNormal("tipoEmbargo", embargo.getTipoEmbargo().toString());
		}
		if (embargo.getMontoAEmbargar() != null) {
			consulta.searchNormal("montoAEmbargar", embargo.getMontoAEmbargar().toString());
		}
		if (!embargo.getNumCuentaAgrario().isEmpty()) {
			consulta.searchNormal("numCuentaAgrario", embargo.getNumCuentaAgrario());
		}
		if (!embargo.getDemandantes().get(tamDemandantes - 1).getIdentificacion().isEmpty()) {
			consulta.searchPersona("demandantes", "identificacion",
					embargo.getDemandantes().get(tamDemandantes - 1).getIdentificacion());
		}
		if (embargo.getDemandantes().get(tamDemandantes - 1).getTipoIdentificacion() != null) {
			consulta.searchPersona("demandantes", "tipoIdentificacion",
					embargo.getDemandantes().get(tamDemandantes - 1).getTipoIdentificacion().toString());
		}
		if (!embargo.getDemandantes().get(tamDemandantes - 1).getNombres().isEmpty()) {
			consulta.searchPersona("demandantes", "nombres",
					embargo.getDemandantes().get(tamDemandantes - 1).getNombres());
		}
		if (!embargo.getDemandantes().get(tamDemandantes - 1).getApellidos().isEmpty()) {
			consulta.searchPersona("demandantes", "apellidos",
					embargo.getDemandantes().get(tamDemandantes - 1).getApellidos());
		}
		if (!embargo.getDemandados().get(tamDemandados - 1).getIdentificacion().isEmpty()) {
			consulta.searchPersona("demandados", "identificacion",
					embargo.getDemandados().get(tamDemandados - 1).getIdentificacion());
		}
		if (embargo.getDemandados().get(tamDemandados - 1).getTipoIdentificacion() != null) {
			consulta.searchPersona("demandados", "tipoIdentificacion",
					embargo.getDemandados().get(tamDemandados - 1).getTipoIdentificacion().toString());
		}
		if (!embargo.getDemandados().get(tamDemandados - 1).getNombres().isEmpty()) {
			consulta.searchPersona("demandados", "nombres",
					embargo.getDemandados().get(tamDemandados - 1).getNombres());
		}
		if (!embargo.getDemandados().get(tamDemandados - 1).getApellidos().isEmpty()) {
			consulta.searchPersona("demandados", "apellidos",
					embargo.getDemandados().get(tamDemandados - 1).getApellidos());
		}
		if (embargo.getDemandados().get(tamDemandados - 1).getMontoAEmbargar() != null) {
			consulta.searchPersona("demandados", "montoAEmbargar",
					embargo.getDemandados().get(tamDemandados - 1).getMontoAEmbargar().toString());
		}
		if (band.equals("gestor")) {
			consulta.searchNormal("username", username);
		} else {
			consulta.searchNormal("idAutoridad", usuarioLogin.getIdAutoridad());
		}
		
		return consulta;
	}

	public EmbargoJudicial jsontoObject(JSONObject jsonRecord) throws JSONException {
		EmbargoJudicial embargoJudicial = new EmbargoJudicial();

		if (jsonRecord.has("idAutoridad")) {
			embargoJudicial.setIdAutoridad(jsonRecord.getString("idAutoridad"));
		}
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
		if (jsonRecord.has("embargoProcesado")) {
			embargoJudicial.setEmbargoProcesado(jsonRecord.getBoolean("embargoProcesado"));
		}
		if (jsonRecord.has("embargado")) {
			embargoJudicial.setEmbargado(jsonRecord.getBoolean("embargado"));
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
