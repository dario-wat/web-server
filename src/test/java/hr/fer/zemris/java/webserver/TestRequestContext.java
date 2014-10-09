package hr.fer.zemris.java.webserver;

import static org.junit.Assert.*;
import hr.fer.zemris.java.webserver.RequestContext.RCCookie;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class TestRequestContext {
	RequestContext rc = null;
	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	Map<String, String> parameters = new HashMap<>();
	Map<String, String> persistentParameters = new HashMap<>();
	List<RCCookie> outputCookies = new ArrayList<>();

	@Test
	public void testRequestContext() {
		rc = new RequestContext(outputStream, parameters, persistentParameters, outputCookies);
		assertTrue("Pogreška u konstruktoru", rc != null);
	}

	@Test
	public void testGetParameter() {
		parameters.put("jedan", "1");
		parameters.put("dva", "2");
		parameters.put("tri", "3");
		rc = new RequestContext(outputStream, parameters, persistentParameters, outputCookies);
		assertTrue("Pogreška u dohvaćanju vrijednosti parametra", rc.getParameter("jedan") == "1");
	}

	@Test
	public void testGetParameterNames() {
		parameters.put("jedan", "1");
		parameters.put("dva", "2");
		parameters.put("tri", "3");
		rc = new RequestContext(outputStream, parameters, persistentParameters, outputCookies);

		Set<String> set = new LinkedHashSet<>();
		set = rc.getParameterNames();
		assertTrue("Pogreška u dohvaćanju imena", set.size() == 3);
	}

	@Test
	public void testGetPersistentParameter() {
		persistentParameters.put("jedan", "1");
		persistentParameters.put("dva", "2");
		persistentParameters.put("tri", "3");
		rc = new RequestContext(outputStream, parameters, persistentParameters, outputCookies);

		assertTrue("Pogreška u dohvaćanju persistentParametara", rc.getPersistentParameter("dva") == "2");
	}

	@Test
	public void testGetPersistentParameterNames() {
		persistentParameters.put("jedan", "1");
		persistentParameters.put("dva", "2");
		persistentParameters.put("tri", "3");
		rc = new RequestContext(outputStream, parameters, persistentParameters, outputCookies);

		Set<String> set = new LinkedHashSet<>();
		set = rc.getPersistentParameterNames();
		assertTrue("Pogreška u dohvaćanju imena persistentParameter", set.size() == 3);
	}

	@Test
	public void testSetPersistentParameter() {
		persistentParameters.put("jedan", "1");
		persistentParameters.put("dva", "2");
		persistentParameters.put("tri", "3");
		rc = new RequestContext(outputStream, parameters, persistentParameters, outputCookies);
		rc.setPersistentParameter("cetiri", "4");
		rc.setPersistentParameter("tri", "novi3");
		assertTrue("Pogreška u dodavanju novog elementa", rc.getPersistentParameter("cetiri") == "4");
		assertTrue("Pogreška u dodavanju mijenjeanju vrijdnosti", rc.getPersistentParameter("tri") == "novi3");
	}

	@Test
	public void testRemovePersistenetParameter() {
		persistentParameters.put("jedan", "1");
		persistentParameters.put("dva", "2");
		persistentParameters.put("tri", "3");
		rc = new RequestContext(outputStream, parameters, persistentParameters, outputCookies);
		rc.removePersistentParameter("jedan");
		assertTrue("Pogreška u brisanju parametara", rc.getPersistentParameter("jedan") == null);
	}

	@Test
	public void testGetTemporaryParameter() {
		rc = new RequestContext(outputStream, parameters, persistentParameters, outputCookies);
		rc.setTemporaryParameter("jedan", "1");
		rc.setTemporaryParameter("dva", "2");
		assertTrue("Pogreška u getTemporaryParameter", rc.getTemporaryParameter("jedan") == "1");
	}

	@Test
	public void testGetTemporaryParameterNames() {
		rc = new RequestContext(outputStream, parameters, persistentParameters, outputCookies);
		rc.setTemporaryParameter("jedan", "1");
		rc.setTemporaryParameter("dva", "2");
		Set<String> set = rc.getTemporaryParameterNames();
		assertTrue("Pogreška u getTemporaryParameterNames", set.size() == 2);
	}

	@Test
	public void testSetTemporaryParameter() {
		rc = new RequestContext(outputStream, parameters, persistentParameters, outputCookies);
		rc.setTemporaryParameter("jedan", "1");
		rc.setTemporaryParameter("dva", "2");
		rc.setTemporaryParameter("jedan", "novi1");
		rc.setTemporaryParameter("tri", "3");
		assertTrue("Pogreška u dodavanju novog elementa", rc.getTemporaryParameter("tri") == "3");
		assertTrue(
				"Pogreška u dodavanju mijenjeanju vrijdnosti",
				rc.getTemporaryParameter("jedan") == "novi1");
	}

	@Test
	public void testRemoveTemporaryParameter() {
		rc = new RequestContext(outputStream, parameters, persistentParameters, outputCookies);
		rc.setTemporaryParameter("jedan", "1");
		rc.setTemporaryParameter("dva", "2");
		rc.removeTemporaryParameter("jedan");
		assertTrue("Pogreška u removeTemporaryParameter", rc.getTemporaryParameter("jedan") == null);
	}

	@Test
	public void testWriteByteArray() throws IOException {
		rc = new RequestContext(outputStream, parameters, persistentParameters, outputCookies);
		String ispis = "Ovo je tekst";
		rc.write(ispis.getBytes());
		String comp = new String(outputStream.toByteArray());
		assertTrue("Pogreška u pisanju bajtova", comp.contains(ispis));
	}

	@Test
	public void testWriteString() throws IOException {
		rc = new RequestContext(outputStream, parameters, persistentParameters, outputCookies);
		String ispis = "Ovo je tekst";
		rc.write(ispis);
		String comp = new String(outputStream.toByteArray());
		assertTrue("Pogreška u pisanju bajtova", comp.contains(ispis));
	}

	@Test
	public void testAddRCCookie() {
		rc = new RequestContext(outputStream, parameters, persistentParameters, outputCookies);
		rc.addRCCookie(new RCCookie("proba", "dosadno", null, "probaDomena", "http://nista:5410"));
	}

	@Test
	public void testGetTemporaryParameters() {
		rc = new RequestContext(outputStream, parameters, persistentParameters, outputCookies);
		Map<String, String> map = new HashMap<>();
		map.put("jedan", "1");
		map.put("dva", "2");
		map.put("eci", "peci");
		rc.setTemporaryParameters(map);
		Map<String, String> temp = rc.getTemporaryParameters();
		assertTrue("Pogreška u GetTemporaryParameters", temp.size() == map.size());
	}

	@Test
	public void testGetPersistentParameters() {
		rc = new RequestContext(outputStream, parameters, persistentParameters, outputCookies);
		Map<String, String> map = null;
		map = rc.getPersistentParameters();
		assertTrue("Pogreška u GetPersistentParameters ", map != null);
	}

	@Test
	public void testSetPersistentParameters() {
		rc = new RequestContext(outputStream, parameters, persistentParameters, outputCookies);
		Map<String, String> map = new HashMap<>();
		map.put("devet", "je sati");
		rc.setPersistentParameters(map);
		assertTrue("Pogreška SetPersistentParameters", rc.getPersistentParameters().get("devet").equals(
				"je sati"));
	}

	@Test
	public void testGetParameters() {
		parameters = new HashMap<>();
		parameters.put("kisa", "pada");
		parameters.put("trava", "rasta");
		rc = new RequestContext(outputStream, parameters, persistentParameters, outputCookies);
		Map<String, String> map = new HashMap<>();
		map = rc.getParameters();
		assertTrue("Pogreška u GetParameters", map.size() == parameters.size());
	}

	@Test
	public void testSetEncoding() throws IOException {
		rc = new RequestContext(outputStream, parameters, persistentParameters, outputCookies);
		rc.setEncoding("UTF-8");
		rc.write("aasdfsadf".getBytes());
		try {
			rc.setEncoding("UTF-8");
		} catch (RuntimeException x) {
			return;
		}
		fail("Nije bačena pogreška kada se postavlja encoding nakon generiranog naslova");
	}

	@Test
	public void testSetStatusCode() throws IOException {
		rc = new RequestContext(outputStream, parameters, persistentParameters, outputCookies);
		rc.setStatusCode(404);
		rc.write("aasdfsadf".getBytes());
		try {
			rc.setStatusCode(404);
		} catch (RuntimeException x) {
			return;
		}
		fail("Nije bačena pogreška kada se postavlja status code nakon generiranog naslova");
	}

	@Test
	public void testSetStatusText() throws IOException {
		rc = new RequestContext(outputStream, parameters, persistentParameters, outputCookies);
		rc.setStatusText("Pogreška");
		rc.write("aasdfsadf".getBytes());
		try {
			rc.setStatusText("Tekst");
		} catch (RuntimeException x) {
			return;
		}
		fail("Nije bačena pogreška kada se postavlja status text nakon generiranog naslova");
	}

	@Test
	public void testSetMimeType() throws IOException {
		rc = new RequestContext(outputStream, parameters, persistentParameters, outputCookies);
		rc.setMimeType("image/jpg");
		rc.write("aasdfsadf".getBytes());
		try {
			rc.setMimeType("text/plain");
		} catch (RuntimeException x) {
			return;
		}
		fail("Nije bačena pogreška kada se postavlja mime type nakon generiranog naslova");
	}

}
