package webserver;

/**
 * Interface for web workers.
 * @author Dario Vidas
 *
 */
public interface IWebWorker {

	/**
	 * Process request for web worker.
	 * @param context request context
	 */
	void processRequest(RequestContext context);
}
