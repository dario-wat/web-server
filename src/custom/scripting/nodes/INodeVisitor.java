package hr.fer.zemris.java.custom.scripting.nodes;

/**
 * Visitor interface for <code>Node</code> class.
 * @author Dario Vidas
 * 
 */
public interface INodeVisitor {

	/**
	 * Visits <code>TextNode</code> object.
	 * @param node node
	 */
	void visitTextNode(TextNode node);

	/**
	 * Visits <code>ForLoopNode</code> object.
	 * @param node node
	 */
	void visitForLoopNode(ForLoopNode node);

	/**
	 * Visits <code>EchoNode</code> object.
	 * @param node node
	 */
	void visitEchoNode(EchoNode node);

	/**
	 * Visits <code>DocumentNode</code> object.
	 * @param node node
	 */
	void visitDocumentNode(DocumentNode node);
}
