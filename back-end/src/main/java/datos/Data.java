package datos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map.Entry;

public class Data {

	private HashMap<Fecha, BigDecimal> historialJudicial;
	private HashMap<Fecha, BigDecimal> historialCoactivo;
	private HashMap<Fecha, BigDecimal> historialSalarioMinimo;

	public Data() {
		super();

		this.historialJudicial = new HashMap<Fecha, BigDecimal>();
		historialJudicial.put(new Fecha(LocalDate.of(2016, 10, 01), LocalDate.of(2017, 9, 30)),
				new BigDecimal(33514152));
		historialJudicial.put(new Fecha(LocalDate.of(2017, 10, 01), LocalDate.of(2018, 9, 30)),
				new BigDecimal(34878187));
		historialJudicial.put(new Fecha(LocalDate.of(2018, 10, 01), LocalDate.of(2019, 9, 30)),
				new BigDecimal(36050085));

		this.historialCoactivo = new HashMap<Fecha, BigDecimal>();
		historialCoactivo.put(new Fecha(LocalDate.of(2016, 10, 01), LocalDate.of(2017, 9, 30)),
				new BigDecimal(16000000));
		historialCoactivo.put(new Fecha(LocalDate.of(2017, 10, 01), LocalDate.of(2018, 9, 30)),
				new BigDecimal(16248000));
		historialCoactivo.put(new Fecha(LocalDate.of(2018, 10, 01), LocalDate.of(2019, 9, 30)),
				new BigDecimal(16910000));

		this.historialSalarioMinimo = new HashMap<Fecha, BigDecimal>();
		historialSalarioMinimo.put(new Fecha(LocalDate.of(2018, 1, 01), LocalDate.of(2019, 12, 01)),
				new BigDecimal(781242));
	}

	public HashMap<Fecha, BigDecimal> getHistorialJudicial() {
		return historialJudicial;
	}

	public void setMontoJudicial(HashMap<Fecha, BigDecimal> historialJudicial) {
		this.historialJudicial = historialJudicial;
	}

	public HashMap<Fecha, BigDecimal> getHistorialCoactivo() {
		return historialCoactivo;
	}

	public void setHistorialCoactivo(HashMap<Fecha, BigDecimal> historialCoactivo) {
		this.historialCoactivo = historialCoactivo;
	}

	public HashMap<Fecha, BigDecimal> getHistorialSalarioMinimo() {
		return historialSalarioMinimo;
	}

	public void setHistorialSalarioMinimo(HashMap<Fecha, BigDecimal> historialSalarioMinimo) {
		this.historialSalarioMinimo = historialSalarioMinimo;
	}

	public BigDecimal getAmountJudicial(LocalDate date) {
		Entry<Fecha, BigDecimal> res = historialJudicial.entrySet().stream()
				.filter(c -> (date.isAfter(c.getKey().getStartDate()) || date.isEqual(c.getKey().getStartDate()))
						&& (date.isBefore(c.getKey().getEndDate()) || date.isEqual(c.getKey().getEndDate())))
				.findAny().orElse(null);
		return res.getValue();
	}

	public BigDecimal getAmountCoactivo(LocalDate date) {
		Entry<Fecha, BigDecimal> res = historialCoactivo.entrySet().stream()
				.filter(c -> (date.isAfter(c.getKey().getStartDate()) || date.isEqual(c.getKey().getStartDate()))
						&& (date.isBefore(c.getKey().getEndDate()) || date.isEqual(c.getKey().getEndDate())))
				.findAny().orElse(null);
		return res.getValue();
	}

	public BigDecimal getAmountSalario(LocalDate date) {
		Entry<Fecha, BigDecimal> res = historialSalarioMinimo.entrySet().stream()
				.filter(c -> (date.isAfter(c.getKey().getStartDate()) || date.isEqual(c.getKey().getStartDate()))
						&& (date.isBefore(c.getKey().getEndDate()) || date.isEqual(c.getKey().getEndDate())))
				.findAny().orElse(null);
		return res.getValue();
	}
}
