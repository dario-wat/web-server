package hr.fer.zemris.java.custom.scripting.tokens;


/**
 * Token koji opisuje stringove unutar tagova.
 * @author Dario
 */
public class TokenString extends Token {


	/**
	 * String.
	 */
	private String value;


	/**
	 * Konstruktor.
	 * @param value string koji je string
	 */
	public TokenString(String value) {
		super();
		this.value = value;
	}


	/**
	 * Getter za string.
	 * @return vraća string string
	 */
	public String getValue() {
		return value;
	}


	/**
	 * Tekstualni oblik klase.
	 * @return vraća string string
	 */
	@Override
	public String toString() {
		return value;
	}
}
