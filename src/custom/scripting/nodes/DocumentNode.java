package hr.fer.zemris.java.custom.scripting.nodes;

/**
 * Klasa Node dokumenta.
 * @author Dario
 */
public class DocumentNode extends Node {

	/**
	 * Dokument u obliku jednog stringa.
	 */
	private String document;

	/**
	 * Konstruktor. Prima string.
	 * @param document string od kojeg pravi dokument
	 */
	public DocumentNode(String document) {
		super();
		this.document = document;
	}
	
	@Override
	public void accept(INodeVisitor visitor) {
		visitor.visitDocumentNode(this);
	}

	/**
	 * Getter za dokument.
	 * @return vraća string dokument
	 */
	public String getDocument() {
		return document;
	}
	
	@Override
	public String toString() {
		return "";
	}
}
