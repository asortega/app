package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;


import enumeraciones.TipoIdentificacion;
import util.Exclude;

public class Demandado extends Persona {

	private BigDecimal montoAEmbargar;
	private BigDecimal montoEmbargado;
	private ArrayList<Intento> intentos;
	@Exclude
	private ArrayList<Cuenta> cuentas;
	private String resEmbargo;
	private LocalDate fechaResolucion;
	
	public Demandado(String identificacion, String nombres, String apellidos, TipoIdentificacion tipoIdentificacion,
			BigDecimal montoAEmbargar) {
		super(identificacion, nombres, apellidos, tipoIdentificacion);
		this.montoAEmbargar = montoAEmbargar;
		this.montoEmbargado = new BigDecimal(0);
		this.intentos=new ArrayList<>();
		this.cuentas=new ArrayList<>();
	}
	
	public Demandado(String identificacion, String nombres, String apellidos, TipoIdentificacion tipoIdentificacion,
			BigDecimal montoAEmbargar,ArrayList<Cuenta> cuentas ) {
		super(identificacion, nombres, apellidos, tipoIdentificacion);
		this.montoAEmbargar = montoAEmbargar;
		this.cuentas=cuentas;
		this.montoEmbargado = new BigDecimal(0);
		this.intentos=new ArrayList<>();
		this.cuentas=new ArrayList<>();
	}
	
	public Demandado() {
		this.montoEmbargado = new BigDecimal(0);
		this.intentos=new ArrayList<>();
		this.cuentas=new ArrayList<>();
	}

	public BigDecimal getMontoAEmbargar() {
		return montoAEmbargar;
	}
	public void setMontoAEmbargar(BigDecimal montoAEmbargar) {
		this.montoAEmbargar = montoAEmbargar;
	}
	public BigDecimal getMontoEmbargado() {
		return montoEmbargado;
	}
	public void setMontoEmbargado(BigDecimal montoEmbargado) {
		this.montoEmbargado = montoEmbargado;
	}
	public ArrayList<Intento> getIntentos() {
		return intentos;
	}
	public void setIntentos(ArrayList<Intento> intentos) {
		this.intentos = intentos;
	}
	public ArrayList<Cuenta> getCuentas() {
		return cuentas;
	}
	public void setCuentas(ArrayList<Cuenta> cuentas) {
		this.cuentas = cuentas;
	}
	
	public String getResEmbargo() {
		return resEmbargo;
	}

	public void setResEmbargo(String resEmbargo) {
		this.resEmbargo = resEmbargo;
	}

	public LocalDate getFechaResolucion() {
		return fechaResolucion;
	}

	public void setFechaResolucion(LocalDate fechaResolucion) {
		this.fechaResolucion = fechaResolucion;
	}
	

}
