package hr.fer.zemris.java.custom.scripting.nodes;

import hr.fer.zemris.java.custom.scripting.tokens.Token;
import hr.fer.zemris.java.custom.scripting.tokens.TokenFunction;
import hr.fer.zemris.java.custom.scripting.tokens.TokenString;

/**
 * Klasa za računske izraze.
 * @author Dario
 */
public class EchoNode extends Node {

	/**
	 * Niz tokena.
	 */
	private Token[] tokens;

	/**
	 * Konstruktor.
	 * @param tokens kolekcija tokena
	 */
	public EchoNode(Token[] tokens) {
		super();
		this.tokens = tokens;
	}

	/**
	 * Getter za niz tokena.
	 * @return niz tokena
	 */
	public Token[] getTokens() {
		return tokens;
	}
	
	@Override
	public void accept(INodeVisitor visitor) {
		visitor.visitEchoNode(this);
	}

	/**
	 * Tekstualni oblik klase.
	 * Format:
	 * [$= token1 token2 ... tokenN$]
	 */
	@Override
	public String toString() {
		StringBuilder build = new StringBuilder();

		build.append("[$=");

		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i] instanceof TokenString) {
				build.append(" \"").append(tokens[i]).append("\"");
			} else if (tokens[i] instanceof TokenFunction) {
				build.append(" @").append(tokens[i]);
			} else {
				build.append(" ").append(tokens[i]);
			}
		}

		build.append("$]");

		return build.toString();
	}
}
