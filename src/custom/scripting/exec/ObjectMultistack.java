package hr.fer.zemris.java.custom.scripting.exec;

import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Razred koji definira kolekciju sličnu Mapi. Objekti se spremaju po ključu, a svaki
 * ključ ima zaseban stog uz koji je vezan.
 * @author Dario Vidas
 */
public class ObjectMultistack {

	private Map<String, MultistackEntry> multistackStorage;

	/**
	 * Konstruktor koji inicijalizira Mapu za spremanje objekata.
	 */
	public ObjectMultistack() {
		super();
		multistackStorage = new HashMap<String, MultistackEntry>();
	}

	/**
	 * Traži ključ, i po ključu dodaje novi objekt na vrh stoga. Ukoliko ključ ne
	 * postoji u kolekciji, stvara novi stog za taj ključ i dodaje ga.
	 * @param name ključ
	 * @param valueWrapper objekt koji dodaje na stog
	 */
	public void push(String name, ValueWrapper valueWrapper) {
		checkKey(name);

		MultistackEntry tempEntry = multistackStorage.get(name);
		if (tempEntry == null) {		//ako ne postoji kljuc, stvaram stog i dodajem
			tempEntry = new MultistackEntry();
			multistackStorage.put(name, tempEntry);
		}
		tempEntry.pushValue(valueWrapper);
	}

	/**
	 * Skida element sa vrha stoga koji je vezan uz dani ključ.
	 * @param name ključ
	 * @return skinuti element ako ključ postoji, <code>null</code> inače
	 */
	public ValueWrapper pop(String name) {
		if (isEmpty(name)) {		//isEmpty provjerava kljuc
			throw new EmptyStackException();
		}

		return multistackStorage.get(name).popValue();
	}

	/**
	 * Čita element sa vrha stoga koji je vezan uz dani ključ. Ne briše element.
	 * @param name ključ
	 * @return učitani element ako ključ postoji, <code>null</code> inače
	 */
	public ValueWrapper peek(String name) {
		if (isEmpty(name)) {		//isEmpty provjerava kljuc
			throw new EmptyStackException();
		}
		return multistackStorage.get(name).peekValue();
	}

	/**
	 * Provjerava je li stog vezan za ključ prazan.
	 * @param name ključ
	 * @return <code>true</code> ako je prazan, <code>false</code> inače
	 */
	public boolean isEmpty(String name) {
		checkKey(name);
		checkKeyExistance(name);
		return multistackStorage.get(name).isEmpty();
	}

	/**
	 * Provjerava je li predani ključ null vrijednost. Ako je, baca exception.
	 * @param name ključ
	 */
	private void checkKey(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Key value cannot be null!");
		}
	}

	/**
	 * Provjerava postoji li dani ključ u mapi. Ako ne postoji baca exception.
	 * @param name ključ
	 */
	private void checkKeyExistance(String name) {
		if (!multistackStorage.containsKey(name)) {
			throw new IllegalArgumentException("Key does not exist!");
		}
	}

	/**
	 * Razred definira objekt kojim se imitira ponašanje stoga.
	 * @author Dario Vidas
	 */
	private static class MultistackEntry {

		private List<ValueWrapper> stack;

		/**
		 * Konstruktor koji inicijalizira listu (stog).
		 */
		public MultistackEntry() {
			super();
			stack = new LinkedList<ValueWrapper>();		//linked list dozvoljava null
		}

		/**
		 * Dodaje element na kraj liste (na vrh stoga).
		 * @param valueWrapper element
		 */
		public void pushValue(ValueWrapper valueWrapper) {
			stack.add(valueWrapper);
		}

		/**
		 * Skida element s kraja liste (vrha stoga).
		 * @return skinuti element
		 */
		public ValueWrapper popValue() {
			ValueWrapper temp = stack.get(stack.size() - 1);
			stack.remove(stack.size() - 1);
			return temp;
		}

		/**
		 * Čita element s kraja liste (vrha stoga), ali ga ne briše.
		 * @return učitani element
		 */
		public ValueWrapper peekValue() {
			return stack.get(stack.size() - 1);
		}

		/**
		 * Provjerava je li lista (stog) prazna.
		 * @return <code>true</code> ako je prazna, <code>false</code> inače
		 */
		public boolean isEmpty() {
			return stack.isEmpty();
		}
	}
}
