package custom.scripting.nodes;

import custom.collections.ArrayBackedIndexedCollection;

/**
 * Klasa koja predstavlja čvor stabla.
 * @author Dario
 */
public abstract class Node {

	/**
	 * Čvor stabla.
	 */
	private ArrayBackedIndexedCollection node;

	/**
	 * Dodaje node dijete roditelju.
	 * @param child node koji dodaje
	 */
	public void addChildNode(Node child) {
		if (child == null) {
			throw new IllegalArgumentException("child ne moze biti null");
		}

		if (node == null) {
			node = new ArrayBackedIndexedCollection(1);
		}

		node.add(child);
	}

	/**
	 * Računa broj djece nekog noda.
	 * @return vraća broj djece, ako je null vraća 0
	 */
	public int numberOfChildren() {
		if (node == null) {
			return 0;
		}
		return node.size();
	}

	/**
	 * Daje dijete sa pozicije index.
	 * @param index pozicija djeteta
	 * @return vraća node dijete
	 */
	public Node getChild(int index) {
		if (index < 0 || index > node.size() - 1) {
			throw new IndexOutOfBoundsException("Ne postoji taj index");
		}

		return (Node) node.get(index);
	}
	
	/**
	 * Support for visitor design.
	 * @param visitor visitor
	 */
	public abstract void accept(INodeVisitor visitor);

	/**
	 * Klasa Node, tekstualna reprezentacija.
	 * @return prazan string
	 */
	@Override
	public abstract String toString();
}
