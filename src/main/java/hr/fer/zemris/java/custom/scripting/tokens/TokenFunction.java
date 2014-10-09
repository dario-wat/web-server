package hr.fer.zemris.java.custom.scripting.tokens;


/**
 * Token za funkcije.
 * @author Dario
 */
public class TokenFunction extends Token {


	/**
	 * Ime funkcije.
	 */
	private String name;


	/**
	 * Konstruktor.
	 * @param name ime funkcije
	 */
	public TokenFunction(String name) {
		super();
		this.name = name;
	}


	/**
	 * Getter za ime funkcije.
	 * @return vraća ime funkcije
	 */
	public String getName() {
		return name;
	}


	/**
	 * Tekstualni oblik klase.
	 * @return vraća ime funkcije
	 */
	@Override
	public String toString() {
		return name;
	}
}
