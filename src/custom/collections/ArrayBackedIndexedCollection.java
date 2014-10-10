package custom.collections;

/**
 * Klasa koja služi za izvođenje raznih
 * operacija nad poljem objekata.
 * @author Dario
 */
public class ArrayBackedIndexedCollection {

	/**
	 * Označava veličinu polja odnosno broj elemenata
	 * u polju.
	 */
	private int size;

	/**
	 * Označava kapacitet polja odnosno broj
	 * elemenata koji stane u polje.
	 */
	private int capacity;

	/**
	 * Polje objekata.
	 */
	private Object[] elements;

	/**
	 * Konstanta koja označava veličinu polja pri pozivu
	 * konstruktora bez argumenata.
	 */
	private static final int POC_KAPACITET = 16;

	/**
	 * Konstruktor bez argumenata koji inicijalno
	 * postavlja veličinu polja na 16. Odmah alocira
	 * mjesta za polje.
	 */
	public ArrayBackedIndexedCollection() {
		size = 0;
		capacity = POC_KAPACITET;
		elements = new Object[capacity];
	}

	/**
	 * Konstruktor koji preko argumenta prima veličinu
	 * polja. Odmah alocira mjesta za polje.
	 * @param initialCapacity Početna veličina polja.
	 */
	public ArrayBackedIndexedCollection(final int initialCapacity) {
		if (initialCapacity < 1) {
			throw new IllegalArgumentException("Kapacitet mora bit > 0");
		}
		size = 0;
		capacity = initialCapacity;
		elements = new Object[capacity];
	}

	/**
	 * Provjerava je li polje prazno.
	 * @return true ako je prazno inače false
	 */
	public boolean isEmpty() {
		if (size == 0) {
			return true;
		}
		return false;
	}

	/**
	 * Daje veličinu polja.
	 * @return veličina polja
	 */
	public int size() {
		return size;
	}

	/**
	 * Pravi kopiju polja.
	 * @param source izvorno polje
	 * @param dest ciljno polje
	 */
	private void copyArray(Object[] source, Object[] dest) {
		for (int i = 0; i < size; i++) {
			dest[i] = source[i];
		}
	}

	/**
	 * Reallocira memoriju polja elements. Krajnja
	 * memorija je duplo veća. Sadržaj ostaje nepromijenjen.
	 */
	private void reallocObjArray() {
		capacity *= 2;
		Object[] tmp = elements;
		elements = new Object[capacity];
		copyArray(tmp, elements);
	}

	/**
	 * Dodaje element na kraj polja. Ukoliko
	 * nema mjesta, proširuje polje za dvostruko.
	 * @param value element koji dodaje
	 */
	public void add(Object value) {
		if (value == null) {
			throw new IllegalArgumentException("value ne moze bit null");
		}

		//ako nema mjesta daj mu još memorije
		if (size == capacity) {
			reallocObjArray();
		}

		elements[size] = value;			//na kraj stavi objekt
		size++;
	}

	/**
	 * Pronalazi element na mjestu index.
	 * @param index pozicija elementa koji traži
	 * @return element na poziciji index
	 */
	public Object get(int index) {
		if (index < 0 || index > size - 1) {
			throw new IndexOutOfBoundsException("Ne postoji taj index");
		}

		return elements[index];
	}

	/**
	 * Briše element iz polja.
	 * @param index pozicija elementa koji briše
	 */
	public void remove(int index) {
		if (index < 0 || index > size - 1) {
			throw new IndexOutOfBoundsException("Ne postoji taj index");
		}

		//pomicem sve elemente
		for (int i = index + 1; i < size; i++) {
			elements[i - 1] = elements[i];
		}

		//gc cisti
		elements[size - 1] = null;
		size--;
	}

	/**
	 * Ubacuje element value na mjesto position.
	 * @param value element koji ubacuje
	 * @param position pozicija na koju ubacuje
	 */
	public void insert(Object value, int position) {
		if (position < 0 || position > size) {
			throw new IndexOutOfBoundsException("Ne postoji ta pozicija");
		}

		if (value == null) {
			throw new IllegalArgumentException("Argument ne može biti null");
		}

		//povecavam polje ako nema mjesta
		if (size == capacity) {
			reallocObjArray();
		}

		//pomicem polje udesno
		for (int i = size; i > position; i--) {
			elements[i] = elements[i - 1];
		}
		elements[position] = value;
		size++;
	}

	/**
	 * Traži element u polju.
	 * @param value element koji traži
	 * @return pozicija unutar polja ako postoji inače -1
	 */
	public int indexOf(Object value) {
		for (int i = 0; i < size; i++) {
			if (elements[i].equals(value)) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Provjerava sadrži li polje neki element.
	 * @param value element koji traži
	 * @return true ako postoji inače false
	 */
	public boolean contains(Object value) {
		for (int i = 0; i < size; i++) {
			if (elements[i].equals(value)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Briše cijelo polje.
	 */
	public void clear() {

		//gc cisti
		for (int i = 0; i < size; i++) {
			elements[i] = null;
		}
		size = 0;
	}
}
