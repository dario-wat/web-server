package custom.scripting.tokens;


/**
 * Double token klasa.
 * @author Dario
 */
public class TokenConstantDouble extends Token {


	/**
	 * Vrijednost double.
	 */
	private double value;


	/**
	 * Konstruktor.
	 * @param value double koji pohranjuje.
	 */
	public TokenConstantDouble(double value) {
		super();
		this.value = value;
	}


	/**
	 * Getter za vrijednost double-a.
	 * @return vraća vrijednost double broja
	 */
	public double getValue() {
		return value;
	}


	/**
	 * Tekstualni oblik klase.
	 * @return vraća double u obliku stringa
	 */
	@Override
	public String toString() {
		return Double.toString(value);
	}
}
