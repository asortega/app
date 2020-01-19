package unicauca.front.end.controllers;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import enumeraciones.Ciudad;
import enumeraciones.Departamento;
import modelo.Autoridad;
import modelo.Embargo;
import modelo.EmbargoJudicial;

@Controller
public class ClienteController {

	@GetMapping("/persona")
	public String persona(Model model) {
		
		//Guardar en clase Usuario
		
		// Buscar el id en la lista de demandados de cada embargo guardado en la
		// blockchain
		ArrayList<Embargo> bdEmbargos = new ArrayList<Embargo>();

		//EmbargoJudicial prueba = new EmbargoJudicial("embargo", "autoridad01", "proceso123", null, null, null, null,
			//	null, null, null);
		//bdEmbargos.add(prueba);// Embargos encontrados con el id

		ArrayList<Autoridad> bdAutoridades = new ArrayList<Autoridad>();
		Departamento departamento = Departamento.values()[ThreadLocalRandom.current().nextInt(0,
				Departamento.values().length)];
		Ciudad ciudad = Ciudad.values()[ThreadLocalRandom.current().nextInt(0, Ciudad.values().length)];
		//Autoridad auto = new Autoridad("autoridad01", null, "JUZGADO PRIMERO", "CALLE 3 #5-18 CENTRO", departamento,
			//	ciudad, null);
		//bdAutoridades.add(auto);// Autoridades encontrados con el id
		model.addAttribute("titulo", "Embargos");
		model.addAttribute("form", "Embargos Aplicados");
		model.addAttribute("bdEmbargos", bdEmbargos);
		model.addAttribute("bdAutoridades", bdAutoridades);

		return "persona/main";
	}

}
