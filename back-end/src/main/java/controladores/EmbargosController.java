package controladores;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import client.CAClient;
import client.ChannelClient;
import client.FabricClient;
import config.Config;
import invocations.QueryChaincode;
import user.UserContext;
import util.Util;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.ChaincodeResponse.Status;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import modelo.Autoridad;
import modelo.Demandado;
import modelo.Embargo;
import modelo.Usuario;
import util.AnnotationExclusionStrategy;

public class EmbargosController {

	static Gson gson = new GsonBuilder().setExclusionStrategies(new AnnotationExclusionStrategy()).create();

	public static void guardarEmbargo(Embargo embargo) {

		String embargoJson = gson.toJson(embargo);
		String[] arguments = { embargoJson };
		Collection<ProposalResponse> responses = Util.createInvoke("crearEmbargo", arguments);
		for (ProposalResponse res : responses) {
			Status status = res.getStatus();
			Logger.getLogger(EmbargosController.class.getName()).log(Level.INFO,
					"Resultado de creacion de embargo: " + status);
		}

	}

	public static void guardarAutoridad(Autoridad autoridad) {
		try {
			
			String autoridadJson=gson.toJson(autoridad);
			System.out.println(autoridadJson);
			String[] arguments = {autoridadJson};	
			Collection<ProposalResponse> responses = Util.createInvoke("crearAutoridad", arguments);
			for (ProposalResponse res: responses) {
				Status status = res.getStatus();
				Logger.getLogger(EmbargosController.class.getName()).log(Level.INFO,
						"Resultado de creacion de autoridad: " + status);
			}
									
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	public static void editarAutoridad(Autoridad autoridad) {
		try {
			
			String autoridadJson=gson.toJson(autoridad);
			System.out.println(autoridadJson);
			String[] arguments = {autoridadJson};	
			Collection<ProposalResponse> responses = Util.createInvoke("editarAutoridad", arguments);
			for (ProposalResponse res: responses) {
				Status status = res.getStatus();
				Logger.getLogger(EmbargosController.class.getName()).log(Level.INFO,
						"Resultado de creacion de autoridad: " + status);
			}
									
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	

	public static void guardarUsuario(Usuario usuario) {
		try {
			
			String usuarioJson=gson.toJson(usuario);
			System.out.println(usuarioJson);
			
			String[] arguments = {usuarioJson};
			System.out.println(arguments);
			
			Collection<ProposalResponse> responses = Util.createInvoke("crearUsuarioSistema", arguments);
			
			for (ProposalResponse res: responses) {
				Status status = res.getStatus();
				Logger.getLogger(EmbargosController.class.getName()).log(Level.INFO,
						"Resultado de creacion de usuario: " + status);
			}
									
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	public static void editarUsuario(Usuario usuario) {
		try {
			
			String usuarioJson=gson.toJson(usuario);
			System.out.println(usuarioJson);
			
			String[] arguments = {usuarioJson};
			System.out.println(arguments);
			
			Collection<ProposalResponse> responses = Util.createInvoke("editarUsuarioSistema", arguments);
			
			for (ProposalResponse res: responses) {
				Status status = res.getStatus();
				Logger.getLogger(EmbargosController.class.getName()).log(Level.INFO,
						"Resultado de creacion de usuario: " + status);
			}
									
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	
	
	public static String obtenerEmbargo(String idEmbargo) {
		String stringResponse = "";
		try {
			String[] arguments = { idEmbargo };
			Collection<ProposalResponse> responses = Util.createQuery("consultarEmbargo", arguments);

			for (ProposalResponse res : responses) {
				Status status = res.getStatus();
				stringResponse += new String(res.getChaincodeActionResponsePayload());				
				Logger.getLogger(EmbargosController.class.getName()).log(Level.INFO,
						"Resultado de consulta  de embargo: " + status);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stringResponse;
	}

	public static String obtenerAutoridad(String idAutoridad) {
		
		String stringResponse = "";
		try {
			String[] arguments = { idAutoridad };
			Collection<ProposalResponse> responses = Util.createQuery("consultarAutoridad", arguments);

			for (ProposalResponse res : responses) {
				Status status = res.getStatus();
				stringResponse += new String(res.getChaincodeActionResponsePayload());				
				Logger.getLogger(EmbargosController.class.getName()).log(Level.INFO,
						"Resultado de consulta  de autoridad: " + status);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stringResponse;
		
	}

	public static String obtenerUsuario(String username) {

	
		String stringResponse = "";
		try {
			String[] arguments = { username };
			Collection<ProposalResponse> responses = Util.createQuery("consultarUsuarioSistema", arguments);

			for (ProposalResponse res : responses) {
				Status status = res.getStatus();
				stringResponse += new String(res.getChaincodeActionResponsePayload());				
				Logger.getLogger(EmbargosController.class.getName()).log(Level.INFO,
						"Resultado de consulta  de usuario: " + status);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stringResponse;
		
	}
	
	public static String obtenerPersona(String idPersona, String tipoIdentificacion) {
		String stringResponse = "";
		try {
			String[] arguments = { idPersona, tipoIdentificacion };
			Collection<ProposalResponse> responses = Util.createQuery("consultarUsuario", arguments);

			for (ProposalResponse res : responses) {
				Status status = res.getStatus();
				stringResponse += new String(res.getChaincodeActionResponsePayload());				
				Logger.getLogger(EmbargosController.class.getName()).log(Level.INFO,
						"Resultado de consulta  de embargo: " + status);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stringResponse;
	}

	public static String consultaGeneral(String consulta) {
		String stringResponse = "";
		try {
			String[] arguments = { consulta };
			Collection<ProposalResponse> responses = Util.createQuery("consultarEmbargoPorCampo", arguments);

			for (ProposalResponse res : responses) {
				Status status = res.getStatus();
				stringResponse += new String(res.getChaincodeActionResponsePayload());				
				Logger.getLogger(EmbargosController.class.getName()).log(Level.INFO,
						"Resultado de consulta  de embargo: " + status);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stringResponse;
	}
}