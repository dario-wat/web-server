package custom.scripting.exec;

import custom.scripting.parser.SmartScriptParser;
import webserver.RequestContext;
import webserver.RequestContext.RCCookie;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class contains demo methods for <code>SmartScriptEngine</code>.
 * @author Dario Vidas
 * 
 */
public class SmartScriptEngineDemos {

	/**
	 * Main program for running smartscript engine.
	 * @param args no arguments
	 */
	public static void main(String[] args) {
		SmartScriptEngineDemos.demo4();
	}

	/**
	 * Test 1.
	 */
	public static void demo1() {
		String documentBody = readFromDisk("osnovni.smscr");
		Map<String, String> parameters = new HashMap<String, String>();
		Map<String, String> persistentParameters = new HashMap<String, String>();
		List<RCCookie> cookies = new ArrayList<RequestContext.RCCookie>();

		// create engine and execute it
		new SmartScriptEngine(new SmartScriptParser(documentBody).getDocumentNode(), new RequestContext(
				System.out,
				parameters,
				persistentParameters,
				cookies)).execute();
	}

	/**
	 * Test 2.
	 */
	public static void demo2() {
		String documentBody = readFromDisk("zbrajanje.smscr");
		Map<String, String> parameters = new HashMap<String, String>();
		Map<String, String> persistentParameters = new HashMap<String, String>();
		List<RCCookie> cookies = new ArrayList<RequestContext.RCCookie>();

		parameters.put("a", "4");
		parameters.put("b", "2");

		// create engine and execute it
		new SmartScriptEngine(new SmartScriptParser(documentBody).getDocumentNode(), new RequestContext(
				System.out,
				parameters,
				persistentParameters,
				cookies)).execute();
	}

	/**
	 * Test 3.
	 */
	public static void demo3() {
		String documentBody = readFromDisk("brojPoziva.smscr");
		Map<String, String> parameters = new HashMap<String, String>();
		Map<String, String> persistentParameters = new HashMap<String, String>();
		List<RCCookie> cookies = new ArrayList<RequestContext.RCCookie>();

		persistentParameters.put("brojPoziva", "3");
		RequestContext rc = new RequestContext(System.out, parameters, persistentParameters, cookies);

		new SmartScriptEngine(new SmartScriptParser(documentBody).getDocumentNode(), rc).execute();
		System.out.println("Vrijednost u mapi: " + rc.getPersistentParameter("brojPoziva"));
	}

	/**
	 * Test 4.
	 */
	public static void demo4() {
		String documentBody = readFromDisk("fibonacci.smscr");
		Map<String, String> parameters = new HashMap<String, String>();
		Map<String, String> persistentParameters = new HashMap<String, String>();
		List<RCCookie> cookies = new ArrayList<RequestContext.RCCookie>();

		// create engine and execute it
		new SmartScriptEngine(new SmartScriptParser(documentBody).getDocumentNode(), new RequestContext(
				System.out,
				parameters,
				persistentParameters,
				cookies)).execute();
	}

	/**
	 * Method reads text file from disk and loads content to string.
	 * @param fileName file name
	 * @return string
	 */
	private static String readFromDisk(String fileName) {
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(fileName), StandardCharsets.UTF_8)) {
			StringBuilder builder = new StringBuilder();
			int bytesRead;
			char[] buffer = new char[1024];

			while ((bytesRead = reader.read(buffer)) != -1) {
				builder.append(buffer, 0, bytesRead);
			}

			return builder.toString();
		} catch (IOException e) {
			System.err.println("Error");		//TODO
			return null;
		}
	}
}
