package hr.fer.zemris.java.custom.collections;

/**
 * Iznimka EmptyStackException.
 * @author Dario
 */
public class EmptyStackException extends RuntimeException {

	/**
	 * Nesto, ne znam.
	 */
	private static final long serialVersionUID = 6307697858263274779L;

	/**
	 * Bez argumenata.
	 */
	public EmptyStackException() {
		super();
	}

	/**
	 * @param message poruka koju ispisuje
	 */
	public EmptyStackException(String message) {
		super(message);
	}

	/**
	 * @param cause uzrok izbacivanja
	 */
	public EmptyStackException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message poruka koju ispisuje
	 * @param cause uzrok izbacivanja
	 */
	public EmptyStackException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * 4 parametra.
	 * @param message poruka koju ispisuje
	 * @param cause uzrok izbacivanja
	 * @param enableSuppression nesto
	 * @param writableStackTrace nesto
	 */
	public EmptyStackException(
			String message,
			Throwable cause,
			boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
