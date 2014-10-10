package custom.scripting.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import hr.fer.zemris.java.custom.scripting.nodes.DocumentNode;
import hr.fer.zemris.java.custom.scripting.nodes.EchoNode;
import hr.fer.zemris.java.custom.scripting.nodes.ForLoopNode;
import hr.fer.zemris.java.custom.scripting.nodes.INodeVisitor;
import hr.fer.zemris.java.custom.scripting.nodes.Node;
import hr.fer.zemris.java.custom.scripting.nodes.TextNode;
import hr.fer.zemris.java.custom.scripting.parser.SmartScriptParser;

/**
 * Program for running smart script parser, and printing output to stdout.
 * @author Dario Vidas
 * 
 */
public class TreeWriter {

	/**
	 * Main program for running parser.
	 * @param args single argument, file name
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Expected one argument, file name.");
			System.exit(0);
		}

		String docBody = null;
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(args[0]), StandardCharsets.UTF_8)) {
			docBody = readFromStream(reader);
		} catch (IOException e) {
			System.err.println("Error while reading file.");
		}

		SmartScriptParser parser = new SmartScriptParser(docBody);
		WriterVisitor visitor = new WriterVisitor();
		parser.getDocumentNode().accept(visitor);
	}

	/**
	 * Reads content from stream and creates string.
	 * @param reader stream reader
	 * @return string
	 * @throws IOException exception while reading
	 */
	private static String readFromStream(BufferedReader reader) throws IOException {
		StringBuilder builder = new StringBuilder();
		char[] buff = new char[1024];

		int bytesRead;
		while (true) {
			bytesRead = reader.read(buff);
			if (bytesRead == -1) {
				break;
			}
			builder.append(buff, 0, bytesRead);
		}

		return builder.toString();
	}

	/**
	 * Private class that prints node content to stdout.
	 * @author Dario Vidas
	 * 
	 */
	private static class WriterVisitor implements INodeVisitor {

		@Override
		public void visitTextNode(TextNode node) {
			runVisitors(node);
		}

		@Override
		public void visitForLoopNode(ForLoopNode node) {
			runVisitors(node);
			System.out.print("[$END$]");
		}

		@Override
		public void visitEchoNode(EchoNode node) {
			runVisitors(node);
		}

		@Override
		public void visitDocumentNode(DocumentNode node) {
			runVisitors(node);
		}

		/**
		 * Runs through child nodes and prints content to stdout.
		 * @param node node to print
		 */
		private void runVisitors(Node node) {
			System.out.print(node);
			final int size = node.numberOfChildren();
			for (int i = 0; i < size; i++) {
				node.getChild(i).accept(this);
			}
		}

	}
}
