package hr.fer.zemris.java.custom.scripting.exec;

/**
 * Objekti koji sadrže podatke i koji idu na stog.
 * @author Dario Vidas
 */
public class ValueWrapper {

	private Object value;

	/**
	 * Konstruktor koji postavlja <code>value</code> na inicijalnu vrijednost.
	 * @param value inicijalna vrijednost
	 */
	public ValueWrapper(Object value) {
		super();
		this.value = value;
	}

	/**
	 * Postavlja vrijednost.
	 * @param value vrijednost
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * Dohvaća vrijednost.
	 * @return vrijednost
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Metoda uvećava vrijednost <code>value</code> za danu vrijednost.
	 * @param incValue vrijednost za koju uvećava
	 */
	public void increment(Object incValue) {
		checkInstances(value);
		checkInstances(incValue);

		//u ovom trenutku objekti mogu biti jedino null, Integer, String, Double
		Number realValue = getNumValue(value);
		Number realArgValue = getNumValue(incValue);

		//u ovom trenutku objekti mogu biti jedino Integer ili Double
		if (realValue instanceof Double || realArgValue instanceof Double) {
			value = Double.valueOf(realValue.doubleValue() + realArgValue.doubleValue());
		} else {
			value = Integer.valueOf(realValue.intValue() + realArgValue.intValue());
		}
	}

	/**
	 * Metoda umanjuje vrijednost <code>value</code> za danu vrijednost.
	 * @param decValue vrijednost za koju umanjuje
	 */
	public void decrement(Object decValue) {
		checkInstances(value);
		checkInstances(decValue);

		Number realValue = getNumValue(value);
		Number realArgValue = getNumValue(decValue);

		if (realValue instanceof Double || realArgValue instanceof Double) {
			value = Double.valueOf(realValue.doubleValue() - realArgValue.doubleValue());
		} else {
			value = Integer.valueOf(realValue.intValue() - realArgValue.intValue());
		}
	}

	/**
	 * Metoda množi vrijednost <code>value</code> s predanom vrijednošću.
	 * @param mulValue vrijednost s kojom množi
	 */
	public void multiply(Object mulValue) {
		checkInstances(value);
		checkInstances(mulValue);

		Number realValue = getNumValue(value);
		Number realArgValue = getNumValue(mulValue);

		if (realValue instanceof Double || realArgValue instanceof Double) {
			value = Double.valueOf(realValue.doubleValue() * realArgValue.doubleValue());
		} else {
			value = Integer.valueOf(realValue.intValue() * realArgValue.intValue());
		}
	}

	/**
	 * Metoda dijeli vrijednost <code>value</code> s predanom vrijednošću. Postoji
	 * mogućnost dijeljenja s nulom. Također ako su oba broja cjelobrojna, rješenje
	 * će biti cjelobrojno.
	 * @param divValue vrijednost s kojom dijeli
	 */
	public void divide(Object divValue) {
		checkInstances(value);
		checkInstances(divValue);

		Number realValue = getNumValue(value);
		Number realArgValue = getNumValue(divValue);

		if (realValue instanceof Double || realArgValue instanceof Double) {
			value = Double.valueOf(realValue.doubleValue() / realArgValue.doubleValue());
		} else {
			value = Integer.valueOf(realValue.intValue() / realArgValue.intValue());
		}
	}

	/**
	 * Metoda uspoređuje vrijednost <code>value</code> s danom vrijednošću.
	 * @param withValue vrijednost s kojom uspoređuje
	 * @return vrijednost 0 ako su jednaki, vrijednost manju od nule ako je
	 *         <code>value</code> manji od <code>withValue</code>, vrijednost veću od
	 *         nule ako je <code>value</code> veći od <code>withValue</code>
	 */
	public int numCompare(Object withValue) {
		checkInstances(value);
		checkInstances(withValue);

		Number realValue = getNumValue(value);
		Number realArgValue = getNumValue(withValue);

		if (realValue instanceof Double || realArgValue instanceof Double) {
			return Double.valueOf(realValue.doubleValue()).compareTo(
					Double.valueOf(realArgValue.doubleValue()));
		} else {
			return Integer.valueOf(realValue.intValue()).compareTo(Integer.valueOf(realArgValue.intValue()));
		}
	}

	/**
	 * Metoda koja pokuša od danog objekta stvoriti numeričku vrijednost (bila ona
	 * cjelobrojna ili decimalna). Ukoliko je predana <code>null</code> referenca
	 * vraća <code>Integer</code> vrijednosti 0. Ukoliko je predani objekt instanca
	 * stringa onda poziva metodu <code>checkString(String)</code>. Ako nije
	 * zadovoljen ni jedan od ova dva uvjeta, metoda će vratiti predani argument.
	 * Argument koji prima može biti null, Integer, Double, String.
	 * @param value vrijednost
	 * @return numeričku vrijednost
	 */
	private Number getNumValue(Object value) {
		if (value == null) {
			return Integer.valueOf(0);
		}
		if (value instanceof String) {
			return numValueOfString((String) value);
		}
		return (Number) value;
	}

	/**
	 * Metoda provjerava je li objekt jednak null ili je tipa Integer, Double ili
	 * String.
	 * @param value objekt
	 */
	private void checkInstances(Object value) {
		if (value != null && !(value instanceof Double) && !(value instanceof Integer)
				&& !(value instanceof String)) {
			throw new IllegalArgumentException(
					"Object must be null or instance of Integer, Double or String!");
		}
	}

	/**
	 * Metoda iz dobivenog stringa pokuša kreirati <code>double</code> ili
	 * <code>integer</code> vrijednost. Ako to uspije vraća odgovarajući tip objekta,
	 * ako ne uspije baca exception.
	 * @param value string
	 * @return vrijednost
	 */
	private Number numValueOfString(String value) {
		if (value.indexOf('.') >= 0 || value.indexOf('E') >= 0) {
			Double numberDouble = getDouble(value);
			if (numberDouble != null) {
				return numberDouble;
			}
		}

		Integer numberInteger = getInteger(value);
		if (numberInteger != null) {
			return numberInteger;
		}

		throw new IllegalArgumentException("Given string does not represent a number!");
	}

	/**
	 * Metoda iz dobivenog stringa pokuša kreirati <code>double</code> vrijednost.
	 * Ako je to moguće vrati <code>Double</code>, inače vraća <code>null</code>.
	 * @param value string
	 * @return <code>double</code> vrijednost ako je to moguće, <code>null</code>
	 *         inače
	 */
	private Double getDouble(String value) {
		double number;
		try {
			number = Double.parseDouble(value);
		} catch (NumberFormatException e) {
			return null;
		}
		return Double.valueOf(number);
	}

	/**
	 * Metoda iz dobivenog stringa pokuša kreirati <code>integer</code> vrijednost.
	 * Ako je to moguće vrati <code>Integer</code>, inače vraća <code>null</code>.
	 * @param value string
	 * @return <code>integer</code> vrijednost ako je to moguće, <code>null</code>
	 *         inače
	 */
	private Integer getInteger(String value) {
		int number;
		try {
			number = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return null;
		}
		return Integer.valueOf(number);
	}

}
