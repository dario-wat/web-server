package custom.scripting.tokens;


/**
 * Token koji opisuje varijable.
 * @author Dario
 */
public class TokenVariable extends Token {


	/**
	 * Ime varijable.
	 */
	private String name;


	/**
	 * Kontruktor koji postavlja ime.
	 * @param name ime koje pridružuje varijabli
	 */
	public TokenVariable(String name) {
		super();
		this.name = name;
	}


	/**
	 * Getter za ime.
	 * @return vraća ime varijable
	 */
	public String getName() {
		return name;
	}


	/**
	 * Tekstualni oblik imena.
	 * @return vraća string ime
	 */
	@Override
	public String toString() {
		return name;
	}
}
