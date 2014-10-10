package custom.scripting.tokens;


/**
 * Integer token klasa.
 * @author Dario
 */
public class TokenConstantInteger extends Token {


	/**
	 * Integer vrijednost.
	 */
	private int value;


	/**
	 * Konstruktor.
	 * @param value vrijednost integera
	 */
	public TokenConstantInteger(int value) {
		super();
		this.value = value;
	}


	/**
	 * Getter za value.
	 * @return vraća vrijednost integera
	 */
	public int getValue() {
		return value;
	}


	/**
	 * Tekstualni oblik klase.
	 * @return vraća integer u obliku stringa
	 */
	@Override
	public String toString() {
		return Integer.toString(value);
	}
}
