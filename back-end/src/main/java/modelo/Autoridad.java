package modelo;

import java.util.ArrayList;

import enumeraciones.Ciudad;
import enumeraciones.Departamento;
import enumeraciones.TipoAutoridad;

public class Autoridad {
	
	private String idAutoridad;
	private TipoAutoridad tipoAutoridad;
	private String nombre;
	private String direccion;
	private Ciudad ciudad;
	private Departamento departamento;
	private String[] embargosRealizados;
	private ArrayList<Usuario> usuarios= new ArrayList<>();
	private boolean habilitado;
	
	
	
	public Autoridad(String idAutoridad, TipoAutoridad tipoAutoridad, String nombre, String direccion, Ciudad ciudad,
			Departamento departamento, ArrayList<Usuario> usuarios) {
		super();
		this.idAutoridad = idAutoridad;
		this.tipoAutoridad = tipoAutoridad;
		this.nombre = nombre;
		this.direccion = direccion;
		this.ciudad = ciudad;
		this.departamento = departamento;
		this.usuarios = usuarios;
	}

	public Autoridad(String idAutoridad, TipoAutoridad tipoAutoridad, String nombre, String direccion,
			String[] embargosRealizados) {
		super();
		this.idAutoridad = idAutoridad;
		this.tipoAutoridad = tipoAutoridad;
		this.nombre = nombre;
		this.direccion = direccion;
		this.embargosRealizados = embargosRealizados;
		this.habilitado=true;
	}
	
	public Autoridad(String idAutoridad, TipoAutoridad tipoAutoridad, String nombre, String direccion) {
		super();
		this.idAutoridad = idAutoridad;
		this.tipoAutoridad = tipoAutoridad;
		this.nombre = nombre;
		this.direccion = direccion;
		this.ciudad=Ciudad.POPAYÁN;
		this.departamento=Departamento.CAUCA;
		this.habilitado=true;
	}
		
	
	public Autoridad() {
		this.habilitado=true;
	}

	public boolean isHabilitado() {
		return habilitado;
	}

	public void setHabilitado(boolean habilitado) {
		this.habilitado = habilitado;
	}

	public String getIdAutoridad() {
		return idAutoridad;
	}
	public void setIdAutoridad(String idAutoridad) {
		this.idAutoridad = idAutoridad;
	}
	
	public TipoAutoridad getTipoAutoridad() {
		return tipoAutoridad;
	}

	public void setTipoAutoridad(TipoAutoridad tipoAutoridad) {
		this.tipoAutoridad = tipoAutoridad;
	}

	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getDireccion() {
		return direccion;
	}
	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}
	public String[] getEmbargosRealizados() {
		return embargosRealizados;
	}
	public void setEmbargosRealizados(String[] embargosRealizados) {
		this.embargosRealizados = embargosRealizados;
	}
	public Ciudad getCiudad() {
		return ciudad;
	}

	public void setCiudad(Ciudad ciudad) {
		this.ciudad = ciudad;
	}

	public Departamento getDepartamento() {
		return departamento;
	}

	public void setDepartamento(Departamento departamento) {
		this.departamento = departamento;
	}

	public ArrayList<Usuario> getUsuarios() {
		return usuarios;
	}

	public void setUsuarios(ArrayList<Usuario> usuarios) {
		this.usuarios = usuarios;
	}
	
	
	
	
	
}
