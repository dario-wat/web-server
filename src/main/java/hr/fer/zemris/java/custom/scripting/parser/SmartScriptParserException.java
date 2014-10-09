package hr.fer.zemris.java.custom.scripting.parser;


/**
 * Klasa iznimke SmartScriptParserException.
 * @author Dario
 */
public class SmartScriptParserException extends RuntimeException {

	/**
	 * Nešto ne znam.
	 */
	private static final long serialVersionUID = -7873703860495022568L;


	/**
	 * Konstruktor bez argumenata.
	 */
	public SmartScriptParserException() {
		super();
	}

	/**
	 * Konstruktor.
	 * @param arg0 string za ispis
	 */
	public SmartScriptParserException(String arg0) {
		super(arg0);
	}

	/**
	 * Konstruktor.
	 * @param arg0 uzrok iznimke
	 */
	public SmartScriptParserException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * Konstruktor.
	 * @param arg0 string ispis
	 * @param arg1 uzrok iznimke
	 */
	public SmartScriptParserException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/**
	 * Konstruktor.
	 * @param arg0 string ispis
	 * @param arg1 uzrok iznimke
	 * @param arg2 nešto
	 * @param arg3 nešto
	 */
	public SmartScriptParserException(String arg0, Throwable arg1,
			boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}
}
