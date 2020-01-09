package modelo;

import enumeraciones.TipoIdentificacion;

public class Persona {
	
	private String identificacion;
	private String nombres;
	private String apellidos;
	private TipoIdentificacion tipoIdentificacion;

	public Persona(String identificacion, String nombres, String apellidos,
			TipoIdentificacion tipoIdentificacion) {
		super();
		this.identificacion = identificacion;
		this.nombres = nombres;
		this.apellidos = apellidos;
		this.tipoIdentificacion = tipoIdentificacion;
	}

	public Persona() {
		// TODO Auto-generated constructor stub
	}

	public String getIdentificacion() {
		return identificacion;
	}

	public void setIdentificacion(String idPersona) {
		this.identificacion = idPersona;
	}

	public TipoIdentificacion getTipoIdentificacion() {
		return tipoIdentificacion;
	}

	public void setTipoIdentificacion(TipoIdentificacion tipoId) {
		this.tipoIdentificacion = tipoId;
	}

	public String getNombres() {
		return nombres;
	}

	public void setNombres(String nombres) {
		this.nombres = nombres;
	}

	public String getApellidos() {
		return apellidos;
	}

	public void setApellidos(String apellidos) {
		this.apellidos = apellidos;
	}

}
