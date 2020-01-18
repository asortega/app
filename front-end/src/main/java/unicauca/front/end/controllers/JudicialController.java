package unicauca.front.end.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.io.IOUtils;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.github.javafaker.Faker;
import com.google.gson.Gson;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.TabSettings;
import com.itextpdf.text.pdf.GrayColor;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;

import controladores.EmbargosController;
import enumeraciones.TipoEmbargo;
import enumeraciones.TipoIdentificacion;
import modelo.Autoridad;
import modelo.Demandado;
import modelo.Demandante;
import modelo.Embargo;
import modelo.EmbargoCoactivo;
import modelo.EmbargoJudicial;
import modelo.Intento;
import modelo.Usuario;
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
	private Authentication authentication;
	private SessionHelper session;
	private Service service;
	private Faker faker;
	private BackEndController backendcontroller = new BackEndController();
	

	public JudicialController() {
		simulacionPasarela = new SimulacionPasarelas();
		session = new SessionHelper();
		service = new Service();
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

	@RequestMapping(value = "/form", method = RequestMethod.POST, params = "action=cargar")
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
		System.out.println("Monto:"+ embargo.getMontoAEmbargar());
		return "redirect:/autoridad/judicial/gestor";
		/*
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
		}*/
	}

	@PostMapping("/gestor/aplicar")
	public String reaplicar(@ModelAttribute(name = "embargo") EmbargoJudicial embargo, Model model,
			RedirectAttributes flash) {

		//System.out.println("Id Autoridad:" + embargo.getIdAutoridad());
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
		
		embargo.setEmbargoProcesado(false);
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();
		//embargo.setIdAutoridad(username);
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
			String mensaje="{\"key\":1,\"Record\":{\"idAutoridad\":\"AUT1\",\"numProceso\":\"PRC1\",\"numOficio\":\"OFC1\",\"fechaOficio\":{\"day\":21,\"month\":11,\"year\":2019},\"tipoEmbargo\":\"JUDICIAL\",\"montoAEmbargar\":54000000,\"numCuentaAgrario\":92345654,\"demandados\":[{\"identificacion\":789,\"nombres\":\"SANTIAGO\",\"apellidos\":\"ORTEGA\",\"tipoIdentificacion\":\"NATURAL\",\"montoAEmbargar\":34500000},{\"identificacion\":678,\"nombres\":\"CARLOS\",\"apellidos\":\"RUIZ\",\"tipoIdentificacion\":\"NATURAL\",\"montoAEmbargar\":34500000}],\"demandantes\":[{\"identificacion\":432,\"tipoIdentificacion\":\"NATURAL\",\"nombres\":\"MIGUEL\",\"apellidos\":\"ROSERO\"},{\"identificacion\":543,\"tipoIdentificacion\":\"NATURAL\",\"nombres\":\"LUIS\",\"apellidos\":\"CRUZ\"}]}},{\"key\":2,\"Record\":{\"idAutoridad\":\"AUT2\",\"numProceso\":\"PRC2\",\"numOficio\":\"OFC2\",\"fechaOficio\":{\"day\":24,\"month\":8,\"year\":2018},\"tipoEmbargo\":\"JUDICIAL\",\"montoAEmbargar\":34000000,\"numCuentaAgrario\":92567432,\"demandados\":[{\"identificacion\":543,\"nombres\":\"JUAN\",\"apellidos\":\"RUIZ\",\"tipoIdentificacion\":\"NATURAL\",\"montoAEmbargar\":14500000},{\"identificacion\":212,\"nombres\":\"DIEGO\",\"apellidos\":\"LOPEZ\",\"tipoIdentificacion\":\"NATURAL\",\"montoAEmbargar\":24500000}],\"demandantes\":[{\"identificacion\":213,\"tipoIdentificacion\":\"NATURAL\",\"nombres\":\"CAMILO\",\"apellidos\":\"FUENTES\"},{\"identificacion\":321,\"tipoIdentificacion\":\"NATURAL\",\"nombres\":\"RICARDO\",\"apellidos\":\"CIFUENTES\"}]}}";
			
			//String mensaje = EmbargosController.consultaGeneral(consulta);
			System.out.println("Mensaje: " + mensaje);
			ArrayList<EmbargoJudicial> embargos=onJson(mensaje);
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
	public String outmsj(@ModelAttribute(name = "embargo") EmbargoJudicial embargo,Model model) {

		
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
	
	/***
	 * 
	 * @param model
	 * @return
	 */
	@GetMapping("/secretario")
	public String judicial(Model model) {
		EmbargoJudicial embargoJudicial = new EmbargoJudicial();
		embargoJudicial.getDemandantes().add(new Demandante());
		embargoJudicial.getDemandados().add(new Demandado());
		model.addAttribute("titulo", "App");
		model.addAttribute("form", "Formulario");
		model.addAttribute("embargoJudicial", embargoJudicial);
		return "autoridad/judicial/secretario/main";
	}

	@RequestMapping(value = "/secretario/form", method = RequestMethod.POST, params = "action=consultar")
	public String judicial(@ModelAttribute(name = "embargoJudicial") EmbargoJudicial embargo, Model model,
			RedirectAttributes flash) throws JSONException {
		Consulta selector = new Consulta();
		if (!consulta(embargo).isEmpty()) {
			selector.setSelector(consulta(embargo));
			Gson gson = new Gson();
			String consulta = gson.toJson(selector);
			System.out.println("Consulta: " + consulta);
			String mensaje="{\"key\":1,\"Record\":{\"idAutoridad\":\"AUT1\",\"numProceso\":\"PRC1\",\"numOficio\":\"OFC1\",\"fechaOficio\":{\"day\":21,\"month\":11,\"year\":2019},\"tipoEmbargo\":\"JUDICIAL\",\"montoAEmbargar\":54000000,\"numCuentaAgrario\":92345654,\"demandados\":[{\"identificacion\":789,\"nombres\":\"SANTIAGO\",\"apellidos\":\"ORTEGA\",\"tipoIdentificacion\":\"NATURAL\",\"montoAEmbargar\":34500000},{\"identificacion\":678,\"nombres\":\"CARLOS\",\"apellidos\":\"RUIZ\",\"tipoIdentificacion\":\"NATURAL\",\"montoAEmbargar\":34500000}],\"demandantes\":[{\"identificacion\":432,\"tipoIdentificacion\":\"NATURAL\",\"nombres\":\"MIGUEL\",\"apellidos\":\"ROSERO\"},{\"identificacion\":543,\"tipoIdentificacion\":\"NATURAL\",\"nombres\":\"LUIS\",\"apellidos\":\"CRUZ\"}]}},{\"key\":2,\"Record\":{\"idAutoridad\":\"AUT2\",\"numProceso\":\"PRC2\",\"numOficio\":\"OFC2\",\"fechaOficio\":{\"day\":24,\"month\":8,\"year\":2018},\"tipoEmbargo\":\"JUDICIAL\",\"montoAEmbargar\":34000000,\"numCuentaAgrario\":92567432,\"demandados\":[{\"identificacion\":543,\"nombres\":\"JUAN\",\"apellidos\":\"RUIZ\",\"tipoIdentificacion\":\"NATURAL\",\"montoAEmbargar\":14500000},{\"identificacion\":212,\"nombres\":\"DIEGO\",\"apellidos\":\"LOPEZ\",\"tipoIdentificacion\":\"NATURAL\",\"montoAEmbargar\":24500000}],\"demandantes\":[{\"identificacion\":213,\"tipoIdentificacion\":\"NATURAL\",\"nombres\":\"CAMILO\",\"apellidos\":\"FUENTES\"},{\"identificacion\":321,\"tipoIdentificacion\":\"NATURAL\",\"nombres\":\"RICARDO\",\"apellidos\":\"CIFUENTES\"}]}}";
			
			//String mensaje = EmbargosController.consultaGeneral(consulta);
			System.out.println("Mensaje: " + mensaje);
			ArrayList<EmbargoJudicial> embargos=onJson(mensaje);
			model.addAttribute("titulo", "Consulta");
			model.addAttribute("form", "Consultas");
			model.addAttribute("embargos", embargos);
			model.addAttribute("boton", "consulta");
			return "autoridad/judicial/secretario/consulta";
		} else {
			flash.addFlashAttribute("warning", "No se puede Consultar, Por favor ingresar el campo a consultar");
			return "redirect:/autoridad/judicial/secretario";
		}
	}
	
	public ArrayList<EmbargoJudicial> onJson(String mensaje) throws JSONException {
		ArrayList<EmbargoJudicial> embargos = new ArrayList<EmbargoJudicial>();
		mensaje = "[" + mensaje + "]";
		JSONArray myjson = new JSONArray(mensaje);
		for (int i = 0; i < myjson.length(); i++) {
			JSONObject jsonRecord = myjson.getJSONObject(i).getJSONObject("Record");
			jsontoObject(jsonRecord).setEmbargoProcesado(true);
			embargos.add(jsontoObject(jsonRecord));
		}
		return embargos;
	}
	
	@GetMapping("/gestor/imprimir")
	public ResponseEntity<byte[]> print(Model model) throws DocumentException, IOException, JSONException {
		authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();
		//Buscar todos los embargos registrados por el username
		
        String filepdf = "file.pdf";
        String mensaje="{\"key\":1,\"Record\":{\"idAutoridad\":\"AUT1\",\"numProceso\":\"PRC1\",\"numOficio\":\"OFC1\",\"fechaOficio\":{\"day\":21,\"month\":11,\"year\":2019},\"tipoEmbargo\":\"JUDICIAL\",\"montoAEmbargar\":54000000,\"numCuentaAgrario\":92345654,\"demandados\":[{\"identificacion\":789,\"nombres\":\"SANTIAGO\",\"apellidos\":\"ORTEGA\",\"tipoIdentificacion\":\"NATURAL\",\"montoAEmbargar\":34500000},{\"identificacion\":678,\"nombres\":\"CARLOS\",\"apellidos\":\"RUIZ\",\"tipoIdentificacion\":\"NATURAL\",\"montoAEmbargar\":34500000}],\"demandantes\":[{\"identificacion\":432,\"tipoIdentificacion\":\"NATURAL\",\"nombres\":\"MIGUEL\",\"apellidos\":\"ROSERO\"},{\"identificacion\":543,\"tipoIdentificacion\":\"NATURAL\",\"nombres\":\"LUIS\",\"apellidos\":\"CRUZ\"}]}},{\"key\":2,\"Record\":{\"idAutoridad\":\"AUT2\",\"numProceso\":\"PRC2\",\"numOficio\":\"OFC2\",\"fechaOficio\":{\"day\":24,\"month\":8,\"year\":2018},\"tipoEmbargo\":\"JUDICIAL\",\"montoAEmbargar\":34000000,\"numCuentaAgrario\":92567432,\"demandados\":[{\"identificacion\":543,\"nombres\":\"JUAN\",\"apellidos\":\"RUIZ\",\"tipoIdentificacion\":\"NATURAL\",\"montoAEmbargar\":14500000},{\"identificacion\":212,\"nombres\":\"DIEGO\",\"apellidos\":\"LOPEZ\",\"tipoIdentificacion\":\"NATURAL\",\"montoAEmbargar\":24500000}],\"demandantes\":[{\"identificacion\":213,\"tipoIdentificacion\":\"NATURAL\",\"nombres\":\"CAMILO\",\"apellidos\":\"FUENTES\"},{\"identificacion\":321,\"tipoIdentificacion\":\"NATURAL\",\"nombres\":\"RICARDO\",\"apellidos\":\"CIFUENTES\"}]}}";
		ArrayList<EmbargoJudicial> embargos=onJson(mensaje);
        
        createPdf(filepdf,embargos);
        
        HttpHeaders headers = new HttpHeaders();
        Path pdfPath = Paths.get(filepdf);
        byte[] pdf = Files.readAllBytes(pdfPath);
        headers.setContentType(MediaType.parseMediaType("application/pdf"));
        headers.add("Content-Disposition", "inline; filename=" + filepdf);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(pdf, headers, HttpStatus.OK);
        
        return response;
	}
	
	public void createPdf(String dest,ArrayList<EmbargoJudicial> embargos) throws FileNotFoundException, DocumentException {
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
            for (Demandante demandante: embargos.get(i).getDemandantes()) {
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
            for (Demandado demandado: embargos.get(i).getDemandados()) {
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
	
	public ArrayList<EmbargoJudicial> getAllEmbargos() {
		ArrayList<EmbargoJudicial> embargos = new ArrayList<EmbargoJudicial>();
		for (int i = 0; i < 2; i++) {
			EmbargoJudicial prueba = (EmbargoJudicial) SimulacionCasos.generarEmbargoNormal();
			embargos.add(prueba);
		}
		return embargos;
	}

	public boolean isValid(EmbargoJudicial embargoJudicial) {
		return !embargoJudicial.getUsername().isEmpty() && !embargoJudicial.getNumProceso().isEmpty()
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
