package hr.fer.zemris.java.webserver;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Instances of this class act as context for communication between client and
 * server..
 * @author Dario Vidas
 * 
 */
public class RequestContext {

	private OutputStream outputStream;
	private Charset charset;
	private String encoding = "UTF-8";
	private int statusCode = 200;
	private String statusText = "OK";
	private String mimeType = "text/html";
	private int length = -1;
	private Map<String, String> parameters;
	private Map<String, String> temporaryParameters = new HashMap<>();
	private Map<String, String> persistentParameters;
	private List<RCCookie> outputCookies;
	private boolean headerGenerated = false;

	/**
	 * Constructor with 4 arguments. Constructs object with given output stream,
	 * parameter map, persistent parameter map, and cookie list.
	 * @param outputStream output stream, cannot be <code>null</code>
	 * @param parameters parameter map, <code>null</code> will be considered as empty
	 * @param persistentParameters persistent parameter map, <code>null</code> will
	 *            be considered as empty
	 * @param outputCookies cookie list, <code>null</code> will be considered as
	 *            empty
	 */
	public RequestContext(
			OutputStream outputStream,
			Map<String, String> parameters,
			Map<String, String> persistentParameters,
			List<RCCookie> outputCookies) {

		super();
		if (outputStream == null) {
			throw new IllegalArgumentException("Output stream cannot be null.");
		}
		this.outputStream = outputStream;

		if (parameters == null) {
			this.parameters = new HashMap<>();
		} else {
			this.parameters = parameters;
		}

		if (persistentParameters == null) {
			this.persistentParameters = new HashMap<>();
		} else {
			this.persistentParameters = persistentParameters;
		}

		if (outputCookies == null) {
			this.outputCookies = new ArrayList<>();
		} else {
			this.outputCookies = outputCookies;
		}
	}

	/**
	 * Method sets encoding. Setting encoding will become unavailable after the first
	 * call of method <code>write</code>.
	 * @param encoding encoding
	 */
	public void setEncoding(String encoding) {
		if (headerGenerated) {
			throw new UnsupportedOperationException("Cannot write to encoding.");
		}
		this.encoding = encoding;
	}

	/**
	 * Method sets status code. Setting status code will become unavailable after the
	 * first call of method <code>write</code>.
	 * @param statusCode
	 */
	public void setStatusCode(int statusCode) {
		if (headerGenerated) {
			throw new UnsupportedOperationException("Cannot write status code.");
		}
		this.statusCode = statusCode;
	}

	/**
	 * Method sets status text. Settig status text will become unavailable after the
	 * first call of method <code>write</code>.
	 * @param statusText status text
	 */
	public void setStatusText(String statusText) {
		if (headerGenerated) {
			throw new UnsupportedOperationException("Cannot write status text.");
		}
		this.statusText = statusText;
	}

	/**
	 * Method sets mime type to given value. Setting mime type will become
	 * unavailable after the first call of method <code>write</code>.
	 * @param mimeType mime type
	 */
	public void setMimeType(String mimeType) {
		if (headerGenerated) {
			throw new UnsupportedOperationException("Cannot write mime type.");
		}
		this.mimeType = mimeType;
	}

	/**
	 * Method sets length to given value. Setting length will become unavailable
	 * after the first call of method <code>write</code>.
	 * @param length file length
	 */
	public void setLength(int length) {
		if (headerGenerated) {
			throw new UnsupportedOperationException("Cannot write length.");
		}
		if (length < 0) {
			throw new IllegalArgumentException("File length cannot be less than zero.");
		}
		this.length = length;
	}

	/**
	 * Method adds cookie to list. Adding cookies will become unavailable after
	 * the first call of method <code>write</code>.
	 * @param cookie cookie to add
	 */
	public void addRCCookie(RCCookie cookie) {
		if (headerGenerated) {
			throw new UnsupportedOperationException("Cannot add cookies.");
		}
		outputCookies.add(cookie);
	}

	/**
	 * Method returns parameters map.
	 * @return reference to map
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * Method returns temporary parameters map.
	 * @return reference to map
	 */
	public Map<String, String> getTemporaryParameters() {
		return temporaryParameters;
	}

	/**
	 * Method sets temporary parameters map to given value.
	 * @param temporaryParameters reference to map
	 */
	public void setTemporaryParameters(Map<String, String> temporaryParameters) {
		this.temporaryParameters = temporaryParameters;
	}

	/**
	 * Method returns persistent parameters map.
	 * @return reference to map
	 */
	public Map<String, String> getPersistentParameters() {
		return persistentParameters;
	}

	/**
	 * Method sets persistent parameters map to given value.
	 * @param persistentParameters reference to map
	 */
	public void setPersistentParameters(Map<String, String> persistentParameters) {
		this.persistentParameters = persistentParameters;
	}

	/**
	 * Method retrieves value under given name. If parameter with given
	 * name doesn't exist, returns null.
	 * @param name name key
	 * @return value if it exists, <code>null</code> otherwise
	 */
	public String getParameter(String name) {
		return parameters.get(name);
	}

	/**
	 * Method creates read-only set of all parameter names.
	 * @return set
	 */
	public Set<String> getParameterNames() {
		return new HashSet<>(parameters.keySet());
	}

	/**
	 * Method retrieves value under given name. If persistent parameter with given
	 * name doesn't exist, returns null.
	 * @param name name key
	 * @return value if it exists, <code>null</code> otherwise
	 */
	public String getPersistentParameter(String name) {
		return persistentParameters.get(name);
	}

	/**
	 * Method creates read-only set of all persistent parameter names.
	 * @return set
	 */
	public Set<String> getPersistentParameterNames() {
		return new HashSet<>(persistentParameters.keySet());
	}

	/**
	 * Adds persistent parameter with given name and value.
	 * @param name name key
	 * @param value value
	 */
	public void setPersistentParameter(String name, String value) {
		persistentParameters.put(name, value);
	}

	/**
	 * Removes persistent parameter under given name.
	 * @param name name key
	 */
	public void removePersistentParameter(String name) {
		persistentParameters.remove(name);
	}

	/**
	 * Method retrieves value under given name. If temporary parameter with given
	 * name doesn't exist, returns null.
	 * @param name name key
	 * @return value if it exists, <code>null</code> otherwise
	 */
	public String getTemporaryParameter(String name) {
		return temporaryParameters.get(name);
	}

	/**
	 * Method creates read-only set of all temporary parameter names.
	 * @return set
	 */
	public Set<String> getTemporaryParameterNames() {
		return new HashSet<>(temporaryParameters.keySet());
	}

	/**
	 * Adds temporary parameter with given name and value.
	 * @param name name key
	 * @param value value of the parameter
	 */
	public void setTemporaryParameter(String name, String value) {
		temporaryParameters.put(name, value);
	}

	/**
	 * Removes temporary parameter under given name key.
	 * @param name name key
	 */
	public void removeTemporaryParameter(String name) {
		temporaryParameters.remove(name);
	}

	/**
	 * Method writes given byte array to output stream.
	 * @param data byte array to write
	 * @return reference to this object
	 * @throws IOException exception while writing to stream
	 */
	public RequestContext write(byte[] data) throws IOException {
		if (!headerGenerated) {
			generateHeader();
		}
		outputStream.write(data);
		return this;
	}

	/**
	 * Method writes given string to output stream. String is coded with the charset
	 * that is set to this object.
	 * @param text text to write
	 * @return reference to this object
	 * @throws IOException exception while writing to stream
	 */
	public RequestContext write(String text) throws IOException {
		if (!headerGenerated) {
			generateHeader();
		}
		outputStream.write(codeString(text, charset));
		return this;
	}

	/**
	 * This method is called first time one of the methods <code>write</code> is
	 * called. This method creates header and sends it to output stream. Every
	 * line ends with \r\n.
	 * Header has this form (variable content is inside brackets):
	 * 
	 * First line:
	 * HTTP/1.1 [statusCode] [statusMessage]
	 * 
	 * Second line:
	 * Content-Type: [mimeType]; charset=[encoding]
	 * --charset tag is optional, it's there only if charset is set
	 * 
	 * Third line:
	 * Content-Length: [length]
	 * 
	 * Other lines have this form:
	 * Set-Cookie: [name]="[value]"; Domain=[domain]; Path=[path]; maxAge=[maxAge]
	 * --any of the domain, path and maxAge values can be dropped if they are null
	 * 
	 * Last line:
	 * \r\n
	 * 
	 * @throws IOException exception while writing to stream
	 */
	private void generateHeader() throws IOException {
		headerGenerated = true;
		charset = Charset.forName(encoding);

		String firstLine = "HTTP/1.1 " + statusCode + " " + statusText + "\r\n";
		outputStream.write(codeString(firstLine));

		String secondLine = "Content-Type: " + mimeType;
		if (mimeType.startsWith("text/")) {
			secondLine += "; charset=" + encoding;
		}
		secondLine += "\r\n";
		outputStream.write(codeString(secondLine));

		if (length != -1) {	//length is not yet set
			String thirdLine = "Content-Length: " + length + "\r\n";
			outputStream.write(codeString(thirdLine));
		}

		for (RCCookie c : outputCookies) {
			String cookieLine = "Set-Cookie: " + c.name + "=\"" + c.value + "\"";

			if (c.domain != null) {
				cookieLine += "; Domain=" + c.domain;
			}

			if (c.path != null) {
				cookieLine += "; Path=" + c.path;
			}

			if (c.maxAge != null) {
				cookieLine += "; maxAge=" + c.maxAge;
			}

			cookieLine += "\r\n";
			outputStream.write(codeString(cookieLine));
		}

		outputStream.write(codeString("\r\n"));
	}

	/**
	 * Codes string to byte using given charset.
	 * @param s string to code
	 * @param charset charset
	 * @return byte array
	 */
	private static byte[] codeString(String s, Charset charset) {
		return s.getBytes(charset);
	}

	/**
	 * Codes string to bytes using <code>ISO_8859_1</code> charset.
	 * @param s string to code
	 * @return byte array
	 */
	private static byte[] codeString(String s) {
		return codeString(s, StandardCharsets.ISO_8859_1);
	}

	/**
	 * Inner class defining cookies. Cookies must have name and value. Domain, path
	 * and max age are optional. Class has single constructor and all fields are
	 * read-only.
	 * @author Dario Vidas
	 */
	public static class RCCookie {

		private String name;
		private String value;
		private Integer maxAge;
		private String domain;
		private String path;

		/**
		 * Constructor for creating cookies. Has 5 arguments.
		 * @param name cookie name, cannot be null
		 * @param value cookie value, cannot be null
		 * @param maxAge max age of cookie
		 * @param domain cookie domain
		 * @param path cookie path
		 */
		public RCCookie(String name, String value, Integer maxAge, String domain, String path) {
			super();
			if (name == null || value == null) {
				throw new IllegalArgumentException("Cookie name or value cannot be null.");
			}
			this.name = name;
			this.value = value;
			this.domain = domain;
			this.path = path;
			this.maxAge = maxAge;
		}

		/**
		 * Getter for cookie name.
		 * @return name
		 */
		public String getName() {
			return name;
		}

		/**
		 * Getter for cookie value.
		 * @return value
		 */
		public String getValue() {
			return value;
		}

		/**
		 * Getter for cookie domain.
		 * @return domain
		 */
		public String getDomain() {
			return domain;
		}

		/**
		 * Getter for cookie path.
		 * @return path
		 */
		public String getPath() {
			return path;
		}

		/**
		 * Getter for max age.
		 * @return max age
		 */
		public Integer getMaxAge() {
			return maxAge;
		}
	}
}
