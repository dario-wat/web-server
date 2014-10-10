package hr.fer.zemris.java.webserver;

import hr.fer.zemris.java.custom.scripting.exec.SmartScriptEngine;
import hr.fer.zemris.java.custom.scripting.parser.SmartScriptParser;
import hr.fer.zemris.java.webserver.RequestContext.RCCookie;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Properties;

/**
 * This class is the Smart Http Server. It runs and serves.
 * @author Dario Vidas
 * 
 */
public class SmartHttpServer {

	private static final int BUFFER_SIZE = 1024;

	private String address;
	private int port;
	private int workerThreads;
	private int sessionTimeout;
	private Map<String, String> mimeTypes = new HashMap<>();
	private Map<String, IWebWorker> workersMap = new HashMap<>();
	private ServerThread serverThread;
	private ExecutorService threadPool;
	private Path documentRoot;
	private Map<String, SessionMapEntry> sessions = new HashMap<>();
	private Random sessionRandom = new Random();

	/**
	 * Public constructor with one argument. Constructor reads from config file and
	 * updates all needed properties.
	 * @param configFileName config file name
	 */
	public SmartHttpServer(String configFileName) {
		try (FileInputStream configFile = new FileInputStream(configFileName)) {
			Properties properties = new Properties();
			properties.load(configFile);

			this.address = properties.getProperty("server.address");
			this.port = Integer.parseInt(properties.getProperty("server.port"));
			this.workerThreads = Integer.parseInt(properties.getProperty("server.workerThreads"));
			this.sessionTimeout = Integer.parseInt(properties.getProperty("session.timeout"));
			this.documentRoot = Paths.get(properties.getProperty("server.documentRoot"));

			loadMimeTypes(properties.getProperty("server.mimeConfig"));
			loadWorkers(properties.getProperty("server.workers"));
		} catch (IOException e) {
			e.printStackTrace();	//Log server exception
		}
	}

	/**
	 * Helper method that loads all mime types from given config file name into map.
	 * @param configFileName config file name
	 * @throws IOException exception while loading properties
	 */
	private void loadMimeTypes(String configFileName) throws IOException {
		try (FileInputStream configFile = new FileInputStream(configFileName)) {
			Properties properties = new Properties();
			properties.load(configFile);

			for (Entry<Object, Object> e : properties.entrySet()) {
				mimeTypes.put(e.getKey().toString(), e.getValue().toString());
			}
		}
	}

	/**
	 * Helper method that loads all <code>IWebWorker</code> objects to workers map.
	 * @param configFileName config file name
	 * @throws IOException exception while loading properties
	 */
	private void loadWorkers(String configFileName) throws IOException {
		try (FileInputStream configFile = new FileInputStream(configFileName)) {
			Properties properties = new Properties();
			properties.load(configFile);

			for (Entry<Object, Object> e : properties.entrySet()) {
				String path = e.getKey().toString();
				String fqcn = e.getValue().toString();

				try {
					Class<?> referenceToClass = this.getClass().getClassLoader().loadClass(fqcn);
					Object newObject = referenceToClass.newInstance();
					IWebWorker iww = (IWebWorker) newObject;

					workersMap.put(path, iww);
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e1) {
					e1.printStackTrace();		//Log server exception
				}
			}
		}
	}

	/**
	 * Starts server thread.
	 */
	protected synchronized void start() {
		if (serverThread == null) {
			serverThread = new ServerThread();
			threadPool = Executors.newFixedThreadPool(workerThreads);
			serverThread.start();
			new SessionCollector().start();
		}
	}

	/**
	 * Stops server thread.
	 */
	protected synchronized void stop() {
		if (serverThread != null) {
			serverThread.interrupt();
			try {
				serverThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();	//Log server exception; this should never happen
			}
			serverThread = null;
			threadPool.shutdown();
		}
	}

	/**
	 * Server thread class. This class extends <code>Thread</code> and acts as main
	 * thread for running server.
	 * @author Dario Vidas
	 * 
	 */
	protected class ServerThread extends Thread {

		private ServerSocket serverSocket;

		/**
		 * Public constructor for initializing server socket.
		 */
		public ServerThread() {
			super();
			try {
				this.serverSocket = new ServerSocket(port);
			} catch (IOException e) {
				e.printStackTrace();	//Log server exception
			}
		}

		@Override
		public void interrupt() {
			super.interrupt();
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();	//Log server exception
			}
		}

		@Override
		public void run() {
			try {
				while (!isInterrupted()) {
					Socket client = serverSocket.accept();
					ClientWorker cw = new ClientWorker(client);
					synchronized (SmartHttpServer.this) {
						threadPool.submit(cw);
					}
				}
			} catch (IOException e) {
				if (isInterrupted()) {	//Server was shut down
					return;
				}
				e.printStackTrace();	//Log server exceptions
			}
		}
	}

	/**
	 * Private class that acts as a thread worker for server clients.
	 * @author Dario Vidas
	 * 
	 */
	private class ClientWorker implements Runnable {

		private static final int SID_LENGTH = 20;

		private Socket csocket;
		private PushbackInputStream istream;
		private OutputStream ostream;
		private String version;
		private String method;
		private Map<String, String> params = new HashMap<>();
		private Map<String, String> permParams = null;
		private List<RCCookie> outputCookies = new ArrayList<>();
		private String sid;

		/**
		 * Worker constructor.
		 * @param csocket client socket
		 */
		public ClientWorker(Socket csocket) {
			super();
			this.csocket = csocket;
		}

		@Override
		public void run() {
			try {
				//open streams
				istream = new PushbackInputStream(csocket.getInputStream());
				ostream = csocket.getOutputStream();

				//load header
				List<String> request = null;
				request = readRequest();

				//check header
				if (request.size() < 1) {
					returnResponseStatus(400);
					return;
				}

				//check first line
				String firstLine = request.get(0);
				String[] argsFirst = firstLine.split(" ");

				method = argsFirst[0];
				version = argsFirst[2];
				if (!method.equals("GET") || !((version.equals("HTTP/1.0")) || version.equals("HTTP/1.1"))) {
					//returnResponseStatus(400);
					return;
				}
				String relativePath = argsFirst[1];

				//split second argument to path and parameters
				String path = relativePath;
				String paramString = null;
				if (relativePath.indexOf('?') > -1) {
					path = relativePath.substring(0, relativePath.indexOf('?'));
					paramString = relativePath.substring(relativePath.indexOf('?') + 1);
				}

				synchronized (sessions) {
					checkSession(request);
				}

				parseParameters(paramString);

				//check for forbidden path
				Path requestedPath = documentRoot.resolve(path.substring(1));
				if (!requestedPath.startsWith(documentRoot)) {
					returnResponseStatus(403);
					return;
				}

				//run /ext/ worker
				if (path.startsWith("/ext/")) {		//sync not needed
					String className = path.substring("/ext/".length());
					runWebWorker(className);
					return;
				}

				//run worker from workers map
				IWebWorker iww = workersMap.get(path);
				if (iww != null) {
					synchronized (iww) {	//sync not really needed for current workers
						iww.processRequest(new RequestContext(ostream, params, permParams, outputCookies));
					}
					return;
				}

				//check is file valid
				if (!Files.exists(requestedPath, LinkOption.NOFOLLOW_LINKS)
						|| !Files.isRegularFile(requestedPath, LinkOption.NOFOLLOW_LINKS)
						|| !Files.isReadable(requestedPath)) {
					returnResponseStatus(404);
					return;
				}

				//creating extension and running smartscript or setting mime type
				String extension = "";
				if (path.lastIndexOf('.') > -1 && path.lastIndexOf('.') != path.length() - 1) {
					extension = path.substring(path.lastIndexOf('.') + 1);
				}

				if (extension.equals("smscr")) {
					runSMSCR(requestedPath);
					return;
				}

				String mimeType = mimeTypes.get(extension);
				if (mimeType == null) {
					mimeType = "application/octet-stream";
				}

				RequestContext rc = new RequestContext(ostream, params, permParams, outputCookies);
				rc.setMimeType(mimeType);
				rc.setStatusCode(200);

				writeToRC(rc, requestedPath);		//and at last simply writing file to browser

			} catch (SocketException e) {
				e.printStackTrace();	//Used for debugging
			} catch (IOException e) {
				e.printStackTrace();	//Log server exception
			} finally {		//Log all exceptions
				try {
					csocket.close();	//automatically closes streams
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		/**
		 * Helper method that fills list with requests. Each request line is a new
		 * list element. All lines are terminated by either <code>\n</code> or
		 * <code>\r</code>.
		 * @return new list
		 * @throws IOException exception while reading stream
		 */
		private List<String> readRequest() throws IOException {
			BufferedReader reader = new BufferedReader(new InputStreamReader(istream, StandardCharsets.UTF_8));

			List<String> temp = new ArrayList<>();
			while (true) {
				String line = reader.readLine();
				if (line == null || line.trim().isEmpty()) {
					break;
				}
				temp.add(line);
			}
			return temp;
		}

		/**
		 * Method checks session for entries. Method tries to find candidate for sid.
		 * If not found, new entry will be created. If found and is still valid,
		 * entry will be updated, if it is not valid, new entry will be created.
		 * Also entry map will be assigned to permanent parameters map. This method
		 * will always be under synchronized block.
		 * @param request list of header lines
		 * 
		 * @see #findCandidate(String)
		 * @see #createNew()
		 * @see #getDomain(List)
		 */
		private void checkSession(List<String> request) {
			String sidCandidate = null;
			for (String s : request) {
				if (s.startsWith("Cookie:")) {
					sidCandidate = findCandidate(s);

					if (sidCandidate != null) {		//candidate found
						break;
					}
				}
			}

			if (sidCandidate == null || !sessions.containsKey(sidCandidate)) {
				createNew(getDomain(request));
			} else {
				sid = sidCandidate;
				SessionMapEntry entry = sessions.get(sid);

				if (new Date().after(new Date(entry.validUntil))) {		//expired
					createNew(getDomain(request));
				} else {
					entry.validUntil = new Date().getTime() + sessionTimeout * 1000;
				}
			}

			SessionMapEntry validEntry = sessions.get(sid);
			permParams = validEntry.map;
		}

		/**
		 * Method parses request cookie line. Tries to find cookie with name "sid".
		 * If it finds that cookie, returns sid value, otherwise returns
		 * <code>null</code>.
		 * @param cookieLine cookie line to parse
		 * @return sid value if it exists, <code>null</code> otehrwise
		 */
		private String findCandidate(String cookieLine) {
			String cookieParams = cookieLine.substring("Cookie:".length()).trim();
			String[] splitted = cookieParams.split(";");

			for (String s : splitted) {
				if (s.startsWith("sid")) {
					return s.substring(s.indexOf('"') + 1, s.lastIndexOf('"'));
				}
			}
			return null;
		}

		/**
		 * Helper method for creating new session entry. Method generates new sid,
		 * creates new session entry, updates sessions map and adds cookie. This
		 * method is always invoked inside snychronized block.
		 * @param domain cookie domain
		 * 
		 * @see #generateSID()
		 */
		private void createNew(String domain) {
			sid = generateSID();
			while (sessions.containsKey(sid)) {		//if generated sid exists
				sid = generateSID();
			}

			SessionMapEntry entry = new SessionMapEntry();
			entry.sid = sid;
			entry.validUntil = new Date().getTime() + sessionTimeout * 1000;
			entry.map = new ConcurrentHashMap<>();

			sessions.put(entry.sid, entry);
			if (domain == null) {
				domain = address;
			}
			outputCookies.add(new RCCookie("sid", entry.sid, null, domain, "/"));
		}

		/**
		 * Helper method for extracting domain from header. If there is no line that
		 * starts with "Host:", method will return <code>null</code>.
		 * @param request header request
		 * @return domain if found, <code>null</code> otherwise
		 */
		private String getDomain(List<String> request) {
			for (String s : request) {
				if (s.startsWith("Host:")) {
					return s.substring("Host:".length(), s.lastIndexOf(':')).trim();
				}
			}
			return null;
		}

		/**
		 * Method generates random SID. Generated SID consists of 20 uppercase
		 * letters by default.
		 * @return random SID
		 */
		private String generateSID() {
			StringBuilder builder = new StringBuilder();
			final int span = 26;
			final int indexA = 65;

			for (int i = 0; i < SID_LENGTH; i++) {
				builder.append((char) (sessionRandom.nextInt(span) + indexA));
			}
			return builder.toString();
		}

		/**
		 * Method runs web worker under given name. This method does not need
		 * synchronization because every time it is invoked, method constructs its
		 * own instance of web worker.
		 * @param className class name of worker
		 * @throws IOException error while writing
		 */
		private void runWebWorker(String className) throws IOException {
			try {
				Class<?> clazz = this.getClass().getClassLoader().loadClass(
						"hr.fer.zemris.java.webserver.workers." + className);
				Object newObject = clazz.newInstance();
				IWebWorker iww = (IWebWorker) newObject;

				iww.processRequest(new RequestContext(ostream, params, permParams, outputCookies));
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				//e.printStackTrace();	//Log server exception
				returnResponseStatus(404);
			}
		}

		/**
		 * Helper method for running smart scripts.
		 * @param scriptName script name (path to script)
		 * @throws IOException exception while reading script
		 */
		private void runSMSCR(Path scriptName) throws IOException {
			try (BufferedReader reader = Files.newBufferedReader(scriptName, StandardCharsets.UTF_8)) {
				String content = readFromStream(reader);

				new SmartScriptEngine(new SmartScriptParser(content).getDocumentNode(), new RequestContext(
						ostream,
						params,
						permParams,
						outputCookies)).execute();
			}
		}

		/**
		 * Helper method that fills parameters map with parameters parsed from
		 * string. Parameter can be <code>null</code>. If parameter is
		 * <code>null</code>, it will be interpreted as no parameters.
		 * @param paramString string containing parameters
		 */
		private void parseParameters(String paramString) {
			if (paramString == null) {
				return;
			}

			String[] paramsSplit = paramString.split("&");
			for (String s : paramsSplit) {
				String[] splitParam = s.split("=");
				if (splitParam.length == 2) {
					params.put(splitParam[0], splitParam[1]);
				}
			}
		}

		/**
		 * Method returns response status to client.
		 * @param statusCode status code
		 * @throws IOException exception while writing to stream
		 */
		private void returnResponseStatus(int statusCode) throws IOException {
			RequestContext rc = new RequestContext(ostream, params, permParams, outputCookies);
			rc.setStatusCode(statusCode);
			rc.setStatusText(getStatusMessage(statusCode));
			rc.setMimeType("text/html");

			rc.write("<html><body>");
			rc.write("<h1>" + getStatusMessage(statusCode) + "</h1>");
			rc.write("</body></html>");
		}

		/**
		 * Gets status message. Currently supported 3 messages: 400, 403, 404.
		 * @param statusCode status code
		 * @return status message if exists, <code>null</code> otherwise
		 */
		private String getStatusMessage(int statusCode) {
			switch (statusCode) {
				case 400:
					return version + " 400 Bad Request";
				case 403:
					return version + " 403 Forbidden";
				case 404:
					return version + " 404 File Not Found";
			}
			return null;
		}

	}

	/**
	 * Private class for session entries.
	 * @author Dario Vidas
	 * 
	 */
	private static class SessionMapEntry {
		String sid;
		long validUntil;
		Map<String, String> map;
	}

	/**
	 * Class defines session collector thread. This thread is daemonic garbage
	 * collector thread that cleans sessions map every 5 minutes.
	 * @author Dario Vidas
	 * 
	 */
	private class SessionCollector extends Thread {

		private static final int SLEEP_TIME = 300;

		/**
		 * Sets thread to daemon.
		 */
		public SessionCollector() {
			super();
			setDaemon(true);
		}

		@Override
		public void run() {
			while (true) {

				synchronized (sessions) {
					Set<Entry<String, SessionMapEntry>> temp = new HashSet<>(sessions.entrySet());
					for (Entry<String, SessionMapEntry> e : temp) {
						if (new Date().after(new Date(e.getValue().validUntil))) {
							sessions.remove(e.getKey());
						}
					}
				}

				try {
					sleep(SLEEP_TIME * 1000);	//Sleep 5 minutes
				} catch (InterruptedException e1) {
					return;
				}
			}
		}
	}

	/**
	 * Reads content from stream and creates string.
	 * @param reader stream reader
	 * @return string
	 * @throws IOException exception while reading
	 */
	private static String readFromStream(BufferedReader reader) throws IOException {
		StringBuilder builder = new StringBuilder();
		char[] buff = new char[BUFFER_SIZE];
		int charsRead;

		while (true) {
			charsRead = reader.read(buff);
			if (charsRead == -1) {
				break;
			}
			builder.append(buff, 0, charsRead);
		}

		return builder.toString();
	}

	/**
	 * Helper method for writing file content to request context.
	 * @param rc request context
	 * @param path path to file
	 * @throws IOException exception while opening or writing
	 */
	private static void writeToRC(RequestContext rc, Path path) throws IOException {
		try (BufferedInputStream inStream = new BufferedInputStream(Files.newInputStream(path));
				ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

			byte[] buffer = new byte[BUFFER_SIZE];
			int length;

			while (true) {
				length = inStream.read(buffer);
				if (length == -1) {
					break;
				}
				bos.write(buffer, 0, length);
			}

			rc.setLength(bos.size());
			rc.write(bos.toByteArray());
		}
	}

	/**
	 * Main program for starting server.
	 * @param args no arguments
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Expected path to server.properties file.");
			System.exit(1);
		}
		SmartHttpServer ser = new SmartHttpServer(args[0]);
		ser.start();
	}
}
