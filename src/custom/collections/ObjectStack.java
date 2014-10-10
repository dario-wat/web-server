package custom.collections;

/**
 * Klasa sadrži razne funkcije za upravljanje stogom.
 * @author Dario
 */
public class ObjectStack {

	/**
	 * Stog nad kojim se izvode operacije.
	 */
	private ArrayBackedIndexedCollection stack;

	/**
	 * Konstruktor koji instancira ArrayBackedIndexedCollection
	 * (stog) objekt.
	 */
	public ObjectStack() {
		stack = new ArrayBackedIndexedCollection();
	}

	/**
	 * Konstruktor koji instancira ArrayBackedIndexedCollection
	 * (stog) objekt. Inicijalnu veličinu prima preko argumenta.
	 * @param initialCapacity
	 *            Inicijalna veličina stoga.
	 */
	public ObjectStack(final int initialCapacity) {
		stack = new ArrayBackedIndexedCollection(initialCapacity);
	}

	/**
	 * Provjerava je li kolekcija prazna.
	 * @return vraća true ako je prazna, inače false.
	 */
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	/**
	 * Veličina stoga.
	 * @return vraća veličinu stoga
	 */
	public int size() {
		return stack.size();
	}

	/**
	 * Stavlja element na vrh stoga.
	 * @param value element koji stavlja
	 */
	public void push(Object value) {
		if (value == null) {
			throw new IllegalArgumentException("value ne može biti null");
		}

		stack.add(value);
	}

	/**
	 * Skida element s vrha stoga.
	 * @return vraća element koji je skinuo
	 */
	public Object pop() {
		if (stack.isEmpty()) {
			throw new EmptyStackException("Stog je prazan");
		}

		int vrh = stack.size() - 1;
		Object tmp = stack.get(vrh);
		stack.remove(vrh);

		return tmp;
	}

	/**
	 * Čita element s vrha stoga, ali ga ne briše.
	 * @return vraća element s vrha stoga
	 */
	public Object peek() {
		if (stack.isEmpty()) {
			throw new EmptyStackException("Stog je prazan");
		}

		int vrh = stack.size() - 1;
		return stack.get(vrh);
	}

	/**
	 * Briše sve elemente sa stoga.
	 */
	public void clear() {
		stack.clear();
	}
}
