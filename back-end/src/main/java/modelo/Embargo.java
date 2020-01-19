package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import enumeraciones.TipoEmbargo;

public class Embargo {
	
	private String idAutoridad;
	private String username;
	private String numProceso;
	private LocalDate fechaOficio;
	private TipoEmbargo tipoEmbargo;
	private String numCuentaAgrario;
	private Boolean embargoProcesado;
	private Boolean embargado;
    private ArrayList<Demandado> demandados= new ArrayList<>();
    
    
	public Embargo(String idAutoridad, String username, String numProceso, LocalDate fechaOficio,
			TipoEmbargo tipoEmbargo, String numCuentaAgrario, Boolean embargoProcesado,
			ArrayList<Demandado> demandados) {
		super();
		this.idAutoridad = idAutoridad;
		this.username = username;
		this.numProceso = numProceso;
		this.fechaOficio = fechaOficio;
		this.tipoEmbargo = tipoEmbargo;
		this.numCuentaAgrario = numCuentaAgrario;
		this.embargoProcesado = false;
		this.demandados = demandados;
	}
	
	public Embargo() {
		this.embargoProcesado = false;
	}
	
	public String getIdAutoridad() {
		return idAutoridad;
	}

	public void setIdAutoridad(String idAutoridad) {
		this.idAutoridad = idAutoridad;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getNumProceso() {
		return numProceso;
	}
	public void setNumProceso(String numProceso) {
		this.numProceso = numProceso;
	}
	public LocalDate getFechaOficio() {
		return fechaOficio;
	}
	public void setFechaOficio(LocalDate fechaOficio) {
		this.fechaOficio = fechaOficio;
	}
	public TipoEmbargo getTipoEmbargo() {
		return tipoEmbargo;
	}
	public void setTipoEmbargo(TipoEmbargo tipoEmbargo) {
		this.tipoEmbargo = tipoEmbargo;
	}
	public String getNumCuentaAgrario() {
		return numCuentaAgrario;
	}
	public void setNumCuentaAgrario(String numCuentaAgrario) {
		this.numCuentaAgrario = numCuentaAgrario;
	}
	public Boolean getEmbargoProcesado() {
		return embargoProcesado;
	}
	public void setEmbargoProcesado(Boolean embargoProcesado) {
		this.embargoProcesado = embargoProcesado;
	}
	public ArrayList<Demandado> getDemandados() {
		return demandados;
	}
	public void setDemandados(ArrayList<Demandado> demandados) {
		this.demandados = demandados;
	}

	


}
