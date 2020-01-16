package modelo;

import java.util.ArrayList;

import enumeraciones.TipoIdentificacion;

public class Usuario extends Persona{
	
	private String username;
	private String password;
	private String confirmPassword;
	private ArrayList<String> roles=new ArrayList<>();
	private String ownedBy;
	private boolean habilitado;
	
	
	public Usuario(String identificacion, String nombres, String apellidos, TipoIdentificacion tipoIdentificacion,
			String username, String password, String confirmPassword, ArrayList<String> roles, 
			boolean habilitado) {
		super(identificacion, nombres, apellidos, tipoIdentificacion);
		this.username = username;
		this.password = password;
		this.confirmPassword = confirmPassword;
		this.roles = roles;		
		this.habilitado = habilitado;
	}
	
	public Usuario(String identificacion, String nombres, String apellidos, TipoIdentificacion tipoIdentificacion,
			String username, ArrayList<String> roles,String ownedBy, boolean habilitado) {
		super(identificacion, nombres, apellidos, tipoIdentificacion);
		this.username = username;
		this.roles = roles;
		this.ownedBy = ownedBy;
		this.habilitado = habilitado;
	}



	public Usuario() {
		this.habilitado = true;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
	
	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public ArrayList<String> getRoles() {
		return roles;
	}

	public void setRoles(ArrayList<String> roles) {
		this.roles = roles;
	}
	
	public String getOwnedBy() {
		return ownedBy;
	}

	public void setOwnedBy(String ownedBy) {
		this.ownedBy = ownedBy;
	}

	public boolean isHabilitado() {
		return habilitado;
	}

	public void setHabilitado(boolean habilitado) {
		this.habilitado = habilitado;
	}

	

	


}
