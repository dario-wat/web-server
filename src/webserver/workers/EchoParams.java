package webserver.workers;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import webserver.IWebWorker;
import webserver.RequestContext;

/**
 * Simple web worker that prints all parameters in browser.
 * @author Dario Vidas
 * 
 */
public class EchoParams implements IWebWorker {

	@Override
	public void processRequest(RequestContext context) {
		Map<String, String> params = context.getParameters();
		context.setMimeType("text/plain");

		for (Entry<String, String> e : params.entrySet()) {
			try {
				context.write(e.getKey() + "=" + e.getValue() + "\n");
			} catch (IOException e1) {
				//Log exceptions...
				e1.printStackTrace();
			}
		}
	}

}
