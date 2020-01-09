package unicauca.front.end.service;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import controladores.EmbargosController;
import modelo.Usuario;
import unicauca.front.end.controllers.BackEndController;


@Service("jpaUserDetailsService")
public class JpaUserDetailsService implements UserDetailsService {

	
	private Logger logger = LoggerFactory.getLogger(JpaUserDetailsService.class);
	

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		Usuario usuario = findUserName(username);//
		UserBuilder builder = null;
		if (usuario == null) {
			logger.error("Error en el Login: no existe el usuario '" + username + "' en el sistema!");
			throw new UsernameNotFoundException("Username: " + username + " no existe en el sistema!");
		} else {
			builder = org.springframework.security.core.userdetails.User.withUsername(username);
			builder.password(new BCryptPasswordEncoder().encode(usuario.getPassword()));
			builder.roles(usuario.getRoles().toArray(new String[usuario.getRoles().size()]));
		}
		return builder.build();

	}

	public Usuario findUserName(String username) {
		// Role role= new Role();
		// List<Role> roles= new ArrayList<>();

		if (username.equalsIgnoreCase("app")) {
			ArrayList<String> roles = new ArrayList<>();
			roles.add("APP");
			// role.setAuthority("ADMIN");
			// roles.add(role);
			Usuario usuario = new Usuario();
			usuario.setUsername(username);
			usuario.setPassword("app");
			usuario.setRoles(roles);
			return usuario;
		} 
		if (username.equalsIgnoreCase("judicial")) {
			ArrayList<String> roles = new ArrayList<>();
			roles.add("GESTOR");
			roles.add("JUDICIAL");
			Usuario usuario = new Usuario();
			usuario.setUsername(username);
			usuario.setPassword("judicial");
			usuario.setRoles(roles);
			return usuario;	
		}
		if (username.equalsIgnoreCase("coactivo")) {
			ArrayList<String> roles = new ArrayList<>();
			roles.add("GESTOR");
			roles.add("COACTIVO");
			Usuario usuario = new Usuario();
			usuario.setUsername(username);
			usuario.setPassword("coactivo");
			usuario.setRoles(roles);
			return usuario;	
		}
		
		/*
		else {
			if (username.equalsIgnoreCase("admin")) {
				ArrayList<String> roles = new ArrayList<>();
				roles.add("ADMIN");
				// role.setAuthority("AUTORIDAD");
				// roles.add(role);
				Usuario usuario = new Usuario();
				usuario.setUsername(username);
				usuario.setPassword("admin");
				usuario.setRoles(roles);
				if (BackEndController.obtenerUsuario(username) == null) {
					EmbargosController.guardarUsuario(usuario);
				}
				return usuario;

			} else {
				if (username.equalsIgnoreCase("judicial")) {
					ArrayList<String> roles = new ArrayList<>();
					roles.add("GESTOR");
					roles.add("JUDICIAL");
					// role.setAuthority("AUTORIDAD");
					// roles.add(role);
					Usuario usuario = new Usuario();
					usuario.setUsername(username);
					usuario.setPassword("judicial");
					usuario.setRoles(roles);
					if (BackEndController.obtenerUsuario(username) == null) {
						EmbargosController.guardarUsuario(usuario);
					}
					return usuario;	
				}else {
					if (username.equalsIgnoreCase("coactivo")) {
						ArrayList<String> roles = new ArrayList<>();
						roles.add("GESTOR");
						roles.add("COACTIVO");
						// role.setAuthority("AUTORIDAD");
						// roles.add(role);
						Usuario usuario = new Usuario();
						usuario.setUsername(username);
						usuario.setPassword("coactivo");
						usuario.setRoles(roles);
						if (BackEndController.obtenerUsuario(username) == null) {
							EmbargosController.guardarUsuario(usuario);
						}
						
						return usuario;
					}
				}
				
			}
		}*/
		return BackEndController.obtenerUsuario(username);
	}

}
