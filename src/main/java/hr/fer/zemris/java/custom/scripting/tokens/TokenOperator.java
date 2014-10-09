package hr.fer.zemris.java.custom.scripting.tokens;


/**
 * Token koji sadrži i opisuje operatore.
 * @author Dario
 */
public class TokenOperator extends Token {


	/**
	 * Simbol koji označava jedan od 4 osnovna operatora.
	 * Operatori:
	 * + * - /
	 */
	private String symbol;


	/**
	 * Konstruktor.
	 * @param value prima operator
	 */
	public TokenOperator(String value) {
		super();
		symbol = value;
	}


	/**
	 * Getter.
	 * @return vraća operator
	 */
	public String getSymbol() {
		return symbol;
	}


	/**
	 * Tekstualni oblik operatora.
	 * @return vraća znak operatora
	 */
	@Override
	public String toString() {
		return symbol;
	}
}
