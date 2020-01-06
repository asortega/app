package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import enumeraciones.TipoEmbargo;

public class EmbargoJudicial extends Embargo{

	private BigDecimal montoAEmbargar;
	private String numOficio;
	private ArrayList<Demandante> demandantes= new ArrayList<>();

	public EmbargoJudicial(String idEmbargo, String idAutoridad, String numProceso, LocalDate fechaOficio,
			TipoEmbargo tipoEmbargo, String numCuentaAgrario, String ciudadCuentaAgrario,
			String departamentoCuentaAgrario, ArrayList<Demandado> demandados,
			BigDecimal montoAEmbargar, String numOficio, ArrayList<Demandante> demandantes) {
		super(idAutoridad, numProceso, fechaOficio, tipoEmbargo, numCuentaAgrario, ciudadCuentaAgrario,
				departamentoCuentaAgrario, demandados);
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
