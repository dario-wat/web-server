package hr.fer.zemris.java.custom.scripting.nodes;

import hr.fer.zemris.java.custom.scripting.tokens.Token;
import hr.fer.zemris.java.custom.scripting.tokens.TokenVariable;

/**
 * Node klasa za for petlju.
 * @author Dario
 */
public class ForLoopNode extends Node {

	/**
	 * Varijabla.
	 */
	private TokenVariable variable;

	/**
	 * Početna vrijednost.
	 */
	private Token startExpression;

	/**
	 * Završna vrijednost.
	 */
	private Token endExpression;

	/**
	 * Korak petlje.
	 */
	private Token stepExpression;

	/**
	 * Konstruktor.
	 * @param variable varijabla
	 * @param startExpression početna vrijednost
	 * @param endExpression završna vrijednost
	 * @param stepExpression korak petlje, može biti null
	 */
	public ForLoopNode(
			TokenVariable variable,
			Token startExpression,
			Token endExpression,
			Token stepExpression) {
		super();
		this.variable = variable;
		this.startExpression = startExpression;
		this.endExpression = endExpression;
		this.stepExpression = stepExpression;
	}

	/**
	 * Getter za varijablu.
	 * @return vraća varijablu
	 */
	public TokenVariable getVariable() {
		return variable;
	}

	/**
	 * Getter za početnu vrijednost.
	 * @return vraća početnu vrijednost
	 */
	public Token getStartExpression() {
		return startExpression;
	}

	/**
	 * Getter za završnu vrijednost.
	 * @return vraća završnu vrijednost
	 */
	public Token getEndExpression() {
		return endExpression;
	}

	/**
	 * Getter za korak petlje.
	 * @return vraća vrijednost koraka
	 */
	public Token getStepExpression() {
		return stepExpression;
	}
	
	@Override
	public void accept(INodeVisitor visitor) {
		visitor.visitForLoopNode(this);
	}

	/**
	 * Tekstualni oblik klase.
	 * Format:
	 * [$FOR variable startExpression endExpression stepExpression&]
	 * stepExpression može biti izostavljen.
	 * @return vraća obrađeni string
	 */
	@Override
	public String toString() {
		StringBuilder build = new StringBuilder();

		build.append("[$FOR");
		build.append(" ").append(variable);
		build.append(" ").append(startExpression);
		build.append(" ").append(endExpression);

		if (stepExpression != null) {
			build.append(" ").append(stepExpression);
		}

		build.append("$]");

		return build.toString();
	}
}
