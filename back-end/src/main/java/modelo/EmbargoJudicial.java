package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import enumeraciones.TipoEmbargo;

public class EmbargoJudicial extends Embargo{
	
	private BigDecimal montoAEmbargar;
	private String numOficio;
	private ArrayList<Demandante> demandantes= new ArrayList<>();
	
	

	public EmbargoJudicial(String idAutoridad, String username, String numProceso, LocalDate fechaOficio,
			TipoEmbargo tipoEmbargo, String numCuentaAgrario, Boolean embargoProcesado, ArrayList<Demandado> demandados,
			BigDecimal montoAEmbargar, String numOficio, ArrayList<Demandante> demandantes) {
		super(idAutoridad, username, numProceso, fechaOficio, tipoEmbargo, numCuentaAgrario, embargoProcesado,
				demandados);
		this.montoAEmbargar = montoAEmbargar;
		this.numOficio = numOficio;
		this.demandantes = demandantes;
	}

	public EmbargoJudicial() {}

	public BigDecimal getMontoAEmbargar() {
		return montoAEmbargar;
	}


	public void setMontoAEmbargar(BigDecimal montoAEmbargar) {
		this.montoAEmbargar = montoAEmbargar;
	}


	public String getNumOficio() {
		return numOficio;
	}


	public void setNumOficio(String numOficio) {
		this.numOficio = numOficio;
	}


	public ArrayList<Demandante> getDemandantes() {
		return demandantes;
	}


	public void setDemandantes(ArrayList<Demandante> demandantes) {
		this.demandantes = demandantes;
	}
	
	
}
