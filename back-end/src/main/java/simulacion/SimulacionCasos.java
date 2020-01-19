package simulacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import enumeraciones.TipoCuenta;
import enumeraciones.TipoEmbargo;
import enumeraciones.TipoIdentificacion;
import modelo.Demandado;
import modelo.DemandadoDian;
import modelo.Demandante;
import modelo.Embargo;
import modelo.EmbargoCoactivo;
import modelo.EmbargoJudicial;
import modelo.Persona;
/*
public class SimulacionCasos {
	
	public static Embargo generarEmbargoNormal() {
	    long minDay = LocalDate.of(2016, 10, 1).toEpochDay();
	    long maxDay = LocalDate.of(2019, 9, 30).toEpochDay();
	    long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
		BigDecimal min=new BigDecimal(0);
		BigDecimal max=new BigDecimal(70000000);
		
		String idEmbargo="embargo"+ThreadLocalRandom.current().nextInt(1, 99999+ 1);
		String idAutoridad="autoridad"+ThreadLocalRandom.current().nextInt(1, 99999+ 1);
		String numProceso=Integer.toString(ThreadLocalRandom.current().nextInt(1, 99999+ 1));
		String numOficio=Integer.toString(ThreadLocalRandom.current().nextInt(1, 99999+ 1));
	    LocalDate fechaOficio = LocalDate.ofEpochDay(randomDay);
	    TipoEmbargo tipoEmbargo= TipoEmbargo.values()[ThreadLocalRandom.current().nextInt(0, TipoEmbargo.values().length)];
	    BigDecimal montoAEmbargar = min.add(new BigDecimal(Math.random()).multiply(max.subtract(min))).setScale(2, BigDecimal.ROUND_HALF_UP);
	    String numCuentaAgrario=Integer.toString(ThreadLocalRandom.current().nextInt(1, 99999+ 1));
	    String ciudadCuentaAgrario="ciudad"+ThreadLocalRandom.current().nextInt(1, 30+ 1);
	    String departamentoCuentaAgrario="departamento"+ThreadLocalRandom.current().nextInt(1, 30+ 1);
		
	    //Generar demandantes
		int randomNum = ThreadLocalRandom.current().nextInt(20, 25+ 1);
		ArrayList<Demandante> demandantes=new ArrayList<>();
		for (int i = 0; i < randomNum; i++) {
			String identificacion=Integer.toString(ThreadLocalRandom.current().nextInt(1, 99999+ 1));
			String nombres="nombre"+ThreadLocalRandom.current().nextInt(1, 50+ 1);
			String apellidos="apellidos"+ThreadLocalRandom.current().nextInt(1, 50+ 1);
			TipoIdentificacion tipoIdentificacion= TipoIdentificacion.values()[ThreadLocalRandom.current().nextInt(0, TipoIdentificacion.values().length)];
			demandantes.add(new Demandante(identificacion, nombres, apellidos, tipoIdentificacion));
		}
		
		//Generar demandados
		randomNum = ThreadLocalRandom.current().nextInt(20, 25+ 1);
		ArrayList<Demandado> demandados=new ArrayList<>();
		for (int i = 0; i < randomNum; i++) {
			String identificacion=Integer.toString(ThreadLocalRandom.current().nextInt(1, 99999+ 1));
			String nombres="nombre"+ThreadLocalRandom.current().nextInt(1, 50+ 1);
			String apellidos="apellidos"+ThreadLocalRandom.current().nextInt(1, 50+ 1);
			TipoIdentificacion tipoIdentificacion= TipoIdentificacion.values()[ThreadLocalRandom.current().nextInt(0, TipoIdentificacion.values().length)];
			BigDecimal monto= montoAEmbargar.divide(new BigDecimal(randomNum)).setScale(2, BigDecimal.ROUND_HALF_UP);
			demandados.add(new Demandado(identificacion, nombres, apellidos, tipoIdentificacion, monto));
		}
		Embargo embargo=new EmbargoJudicial(idEmbargo, idAutoridad, numProceso, fechaOficio, tipoEmbargo, numCuentaAgrario, ciudadCuentaAgrario, departamentoCuentaAgrario, demandados, montoAEmbargar, numOficio, demandantes);	   
	    return embargo;
	}
	
	public static Embargo generarEmbargoDian() {
	    long minDay = LocalDate.of(2016, 10, 1).toEpochDay();
	    long maxDay = LocalDate.of(2019, 9, 30).toEpochDay();
	    long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
		BigDecimal min=new BigDecimal(0);
		BigDecimal max=new BigDecimal(70000000);
		
		String idEmbargo="embargo"+ThreadLocalRandom.current().nextInt(1, 99999+ 1);
		String idAutoridad="autoridad"+ThreadLocalRandom.current().nextInt(1, 99999+ 1);
		String numProceso=Integer.toString(ThreadLocalRandom.current().nextInt(1, 99999+ 1));
		String numOficio=Integer.toString(ThreadLocalRandom.current().nextInt(1, 99999+ 1));
	    LocalDate fechaOficio = LocalDate.ofEpochDay(randomDay);
	    TipoEmbargo tipoEmbargo= TipoEmbargo.values()[ThreadLocalRandom.current().nextInt(0, TipoEmbargo.values().length)];	    
	    String numCuentaAgrario=Integer.toString(ThreadLocalRandom.current().nextInt(1, 99999+ 1));
	    String ciudadCuentaAgrario="ciudad"+ThreadLocalRandom.current().nextInt(1, 30+ 1);
	    String departamentoCuentaAgrario="departamento"+ThreadLocalRandom.current().nextInt(1, 30+ 1);
		
		
		//Generar demandados
		int randomNum = ThreadLocalRandom.current().nextInt(20, 25 + 1);
		ArrayList<Demandado> demandados=new ArrayList<>();
		for (int i = 0; i < randomNum; i++) {
			String identificacion=Integer.toString(ThreadLocalRandom.current().nextInt(1, 99999+ 1));
			String nombres="nombre"+ThreadLocalRandom.current().nextInt(1, 50+ 1);
			String apellidos="apellidos"+ThreadLocalRandom.current().nextInt(1, 50+ 1);
			String resEmbargo="resembargo"+ThreadLocalRandom.current().nextInt(1, 50+ 1);
			LocalDate fechaResolucion = LocalDate.ofEpochDay(randomDay);
			TipoIdentificacion tipoIdentificacion= TipoIdentificacion.values()[ThreadLocalRandom.current().nextInt(0, TipoIdentificacion.values().length)];
			BigDecimal monto = min.add(new BigDecimal(Math.random()).multiply(max.subtract(min))).setScale(2, BigDecimal.ROUND_HALF_UP);
			demandados.add(new DemandadoDian(identificacion, nombres, apellidos, tipoIdentificacion, monto,resEmbargo,fechaResolucion));
		}
		//Embargo embargo= new EmbargoCoactivo(idEmbargo, idAutoridad, numProceso, fechaOficio, tipoEmbargo, numCuentaAgrario, ciudadCuentaAgrario, departamentoCuentaAgrario, demandados);
		Embargo embargo= new EmbargoCoactivo(idEmbargo, idAutoridad, numProceso, fechaOficio, tipoEmbargo, numCuentaAgrario, ciudadCuentaAgrario, departamentoCuentaAgrario, demandados, numOficio);
	    return embargo;
		
	}
	
}*/
