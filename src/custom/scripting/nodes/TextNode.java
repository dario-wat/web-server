package hr.fer.zemris.java.custom.scripting.nodes;

/**
 * Node klasa koja sadrži tekst.
 * @author Dario
 */
public class TextNode extends Node {

	/**
	 * Sadržaj teksta.
	 */
	private String text;

	/**
	 * Konstruktor klase.
	 * @param text tekst koji sprema
	 */
	public TextNode(String text) {
		this.text = text;
	}

	/**
	 * Getter teksta.
	 * @return vraća string text
	 */
	public String getText() {
		return text;
	}
	
	@Override
	public void accept(INodeVisitor visitor) {
		visitor.visitTextNode(this);
	}

	/**
	 * Tekstualni oblik varijable text.
	 */
	@Override
	public String toString() {
		return text;
	}
}
