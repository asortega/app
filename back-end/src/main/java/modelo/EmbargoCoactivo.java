package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import enumeraciones.TipoEmbargo;

public class EmbargoCoactivo extends Embargo {

	private String numOficio;

	
	
	
	public EmbargoCoactivo(String idAutoridad, String username, String numProceso, LocalDate fechaOficio,
			TipoEmbargo tipoEmbargo, String numCuentaAgrario, Boolean embargoProcesado, ArrayList<Demandado> demandados,
			String numOficio) {
		super(idAutoridad, username, numProceso, fechaOficio, tipoEmbargo, numCuentaAgrario, embargoProcesado,
				demandados);
		this.numOficio = numOficio;
	}

	public EmbargoCoactivo() {}
	
	public String getNumOficio() {
		return numOficio;
	}

	public void setNumOficio(String numOficio) {
		this.numOficio = numOficio;
	}

}
