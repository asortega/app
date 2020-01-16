package unicauca.front.end.controllers;

import com.google.gson.Gson;

import controladores.EmbargosController;
import modelo.Autoridad;
import modelo.Usuario;


public class BackEndController {

	public static Usuario obtenerUsuario(String username) {
		Usuario usuario=null;
		
		//String json= EmbargosController.obtenerUsuario(username);
		String json="{\"identificacion\":123,\"tipoIdentificacion\":\"NATURAL\",\"nombres\":\"santiago\",\"apellidos\":\"ortega\",\"username\":\"as\",\"password\":123,\"confirmPassword\":123}";
		if(!json.isEmpty()) {
			System.out.println("Json: " + json);
			usuario = new Gson().fromJson(json, Usuario.class);
			System.out.println("Usuario: " + usuario);
		}
		return usuario;
	}
	
	public static Autoridad obtenerAutoridad(String idAutoridad) {
		String json="{\"idAutoridad\":\"AUT1\",\"nombre\":\"JUZGADO1\",\"tipoAutoridad\":\"JUDICIAL\",\"direccion\":\"Calle 2 # 489\",\"departamento\":\"CAUCA\",\"ciudad\":\"POPAYÁN\",\"usuarios\":[{\"identificacion\":789,\"tipoIdentificacion\":\"NATURAL\",\"nombres\":\"santiago\",\"apellidos\":\"ortega\",\"username\":\"as\",\"password\":123,\"confirmPassword\":123},{\"identificacion\":678,\"tipoIdentificacion\":\"NATURAL\",\"nombres\":\"carlos\",\"apellidos\":\"ruiz\",\"username\":\"cr\",\"password\":123,\"confirmPassword\":123}]}";
		//String json= EmbargosController.obtenerAutoridad(idAutoridad);
		
		Autoridad autoridad=null;
		if(!json.isEmpty()) {
			System.out.println("Json: " + json);
			autoridad = new Gson().fromJson(json, Autoridad.class);
			System.out.println("Autoridad: " + autoridad);
		}
		return autoridad;
	}
	
}
