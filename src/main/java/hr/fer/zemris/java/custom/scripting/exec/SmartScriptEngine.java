package hr.fer.zemris.java.custom.scripting.exec;

import java.io.IOException;
import java.text.DecimalFormat;
import hr.fer.zemris.java.custom.scripting.nodes.DocumentNode;
import hr.fer.zemris.java.custom.scripting.nodes.EchoNode;
import hr.fer.zemris.java.custom.scripting.nodes.ForLoopNode;
import hr.fer.zemris.java.custom.scripting.nodes.INodeVisitor;
import hr.fer.zemris.java.custom.scripting.nodes.TextNode;
import hr.fer.zemris.java.custom.scripting.tokens.Token;
import hr.fer.zemris.java.custom.scripting.tokens.TokenConstantDouble;
import hr.fer.zemris.java.custom.scripting.tokens.TokenConstantInteger;
import hr.fer.zemris.java.custom.scripting.tokens.TokenFunction;
import hr.fer.zemris.java.custom.scripting.tokens.TokenOperator;
import hr.fer.zemris.java.custom.scripting.tokens.TokenString;
import hr.fer.zemris.java.custom.scripting.tokens.TokenVariable;
import hr.fer.zemris.java.webserver.RequestContext;

/**
 * Program for executing smart script codes.
 * @author Dario Vidas
 * 
 */
public class SmartScriptEngine {

	private DocumentNode documentNode;
	private RequestContext requestContext;
	private ObjectMultistack multistack = new ObjectMultistack();

	/**
	 * Key for temporary stack based on <code>ObjectMultiStack</code>.
	 */
	private static final String TEMPKEY = "temp";

	/**
	 * Defines visitor implementing class <code>INodeVisitor</code>. Visitor runs
	 * through nodes and executes commands.
	 */
	private INodeVisitor visitor = new INodeVisitor() {

		@Override
		public void visitTextNode(TextNode node) {
			try {
				requestContext.write(node.getText());
			} catch (IOException e) {
				System.err.println("Text node writing exception.");
			}
		}

		@Override
		public void visitForLoopNode(ForLoopNode node) {
			String variableName = node.getVariable().getName();

			Token startExp = node.getStartExpression();
			Number initialValue = getTokenValue(startExp);
			multistack.push(variableName, new ValueWrapper(initialValue));

			Token endExp = node.getEndExpression();
			Number endValue = getTokenValue(endExp);

			Token stepExp = node.getStepExpression();
			Number stepValue = getTokenValue(stepExp);

			ValueWrapper current = null;
			while ((current = multistack.peek(variableName)).numCompare(endValue) <= 0) {
				final int size = node.numberOfChildren();
				for (int i = 0; i < size; i++) {
					node.getChild(i).accept(this);
				}
				current.increment(stepValue);
			}

			multistack.pop(variableName);
		}

		@Override
		public void visitEchoNode(EchoNode node) {
			ObjectMultistack tempStack = new ObjectMultistack();

			for (Token t : node.getTokens()) {
				executeToken(t, tempStack);
			}

			ObjectMultistack reverseStack = reverseStack(tempStack);

			try {
				printStack(reverseStack);
			} catch (IOException e) {
				System.err.println("Echo node writing exception.");
			}
		}

		@Override
		public void visitDocumentNode(DocumentNode node) {
			final int size = node.numberOfChildren();
			for (int i = 0; i < size; i++) {
				node.getChild(i).accept(this);
			}
		}
	};

	/**
	 * Constructor with 2 arguments.
	 * @param documentNode reference to document node
	 * @param requestContext reference to request context
	 */
	public SmartScriptEngine(DocumentNode documentNode, RequestContext requestContext) {
		super();
		if (documentNode == null) {
			throw new IllegalArgumentException("Document node cannot be null.");
		}
		if (requestContext == null) {
			throw new IllegalArgumentException("Request context cannot be null.");
		}

		this.documentNode = documentNode;
		this.requestContext = requestContext;
	}

	/**
	 * Method executes program written in document node using visitor.
	 */
	public void execute() {
		documentNode.accept(visitor);
	}

	/**
	 * Method determines the type of given token. For each type, method executes it's
	 * "operation" with stack. In general there are 3 types of tokens: constants,
	 * functions/operations and variables. Method pushes contants to stack, executes
	 * functions and operations and pushes variable values to stack.
	 * @param t token
	 * @param tempStack temporary stack, key used for this stack is "temp"
	 */
	protected final void executeToken(Token t, ObjectMultistack tempStack) {

		// using return for prettier code
		if (t instanceof TokenConstantInteger) {
			tempStack.push(TEMPKEY, new ValueWrapper(((TokenConstantInteger) t).getValue()));
			return;
		}

		if (t instanceof TokenConstantDouble) {
			tempStack.push(TEMPKEY, new ValueWrapper(((TokenConstantDouble) t).getValue()));
			return;
		}

		if (t instanceof TokenString) {
			tempStack.push(TEMPKEY, new ValueWrapper(((TokenString) t).getValue()));
			return;
		}

		if (t instanceof TokenFunction) {
			String name = ((TokenFunction) t).getName();
			executeFunction(name, tempStack);
			return;
		}

		if (t instanceof TokenOperator) {
			String operator = ((TokenOperator) t).getSymbol();
			executeOperation(operator, tempStack);
			return;
		}

		if (t instanceof TokenVariable) {
			String varName = ((TokenVariable) t).getName();
			Object value = multistack.peek(varName).getValue();
			if (value == null) {
				throw new IllegalArgumentException("Variable doesn't exist on stack.");
			}
			tempStack.push(TEMPKEY, new ValueWrapper(value));
		}
	}

	/**
	 * Method gets value from given token. Value can be either <code>double</code> or
	 * <code>int</code>. Given token must be subclass of either
	 * <code>TokenConstantDouble</code> or <code>TokenConstantInteger</code>. If
	 * token is <code>null</code> method returns 1.
	 * @param token token
	 * @return token value
	 */
	protected final Number getTokenValue(Token token) {
		if (token == null) {
			return 1;
		}

		if (token instanceof TokenConstantDouble) {
			return ((TokenConstantDouble) token).getValue();
		}

		if (token instanceof TokenConstantInteger) {
			return ((TokenConstantInteger) token).getValue();
		}

		throw new IllegalArgumentException(
				"Token must be instance of TokenConstantDouble or TokenConstantInteger class.");
	}

	/**
	 * Method executes function with given name using parameters from stack. Current
	 * supported functions are: sin, decfmt, dup, swap, setMimeType, paramGet,
	 * pparamGet, pparamSet, pparamDel, tparamGet, tparamSet, tparamDel.
	 * @param name function name
	 * @param stack stack
	 */
	protected final void executeFunction(String name, ObjectMultistack stack) {
		ValueWrapper x = null;
		ValueWrapper y = null;

		switch (name) {
			case "sin":
				x = stack.pop(TEMPKEY);
				x.setValue(Math.sin(((Number) x.getValue()).doubleValue()));
				stack.push(TEMPKEY, new ValueWrapper(x.getValue()));
				break;
			case "decfmt":
				y = stack.pop(TEMPKEY);		//format
				x = stack.pop(TEMPKEY);		//number
				DecimalFormat format = new DecimalFormat(y.getValue().toString());
				x.setValue(format.format(x.getValue()));
				stack.push(TEMPKEY, new ValueWrapper(x.getValue()));
				break;
			case "dup":
				x = stack.pop(TEMPKEY);
				stack.push(TEMPKEY, new ValueWrapper(x.getValue()));
				stack.push(TEMPKEY, new ValueWrapper(x.getValue()));
				break;
			case "swap":
				x = stack.pop(TEMPKEY);
				y = stack.pop(TEMPKEY);
				stack.push(TEMPKEY, new ValueWrapper(x.getValue()));
				stack.push(TEMPKEY, new ValueWrapper(y.getValue()));
				break;
			case "setMimeType":
				x = stack.pop(TEMPKEY);
				requestContext.setMimeType(x.getValue().toString());
				break;
			case "paramGet":
				x = stack.pop(TEMPKEY);		//defValue
				y = stack.pop(TEMPKEY);		//name
				String value = requestContext.getParameter(y.getValue().toString());
				stack.push(TEMPKEY, new ValueWrapper(value == null ? x.getValue() : value));
				break;
			case "pparamGet":
				x = stack.pop(TEMPKEY);		//defValue
				y = stack.pop(TEMPKEY);		//name
				value = requestContext.getPersistentParameter(y.getValue().toString());
				stack.push(TEMPKEY, new ValueWrapper(value == null ? x.getValue() : value));
				break;
			case "pparamSet":
				x = stack.pop(TEMPKEY);		//name
				y = stack.pop(TEMPKEY);		//value
				requestContext.setPersistentParameter(x.getValue().toString(), y.getValue().toString());
				break;
			case "pparamDel":
				x = stack.pop(TEMPKEY);		//name
				requestContext.removePersistentParameter(x.getValue().toString());
				break;
			case "tparamGet":
				x = stack.pop(TEMPKEY);		//defValue
				y = stack.pop(TEMPKEY);		//name
				value = requestContext.getTemporaryParameter(y.getValue().toString());
				stack.push(TEMPKEY, new ValueWrapper(value == null ? x.getValue() : value));
				break;
			case "tparamSet":
				x = stack.pop(TEMPKEY);		//name
				y = stack.pop(TEMPKEY);		//value
				requestContext.setTemporaryParameter(x.getValue().toString(), y.getValue().toString());
				break;
			case "tparamDel":
				x = stack.pop(TEMPKEY);		//name
				requestContext.removeTemporaryParameter(x.getValue().toString());
				break;
			default:
				throw new IllegalArgumentException("Illegal function.");
		}
	}

	/**
	 * Method executes operation. Pops 2 values from stack and executes operation.
	 * Current supported operations are: +, -, * and /.
	 * @param operator operator
	 * @param stack stack
	 */
	protected final void executeOperation(String operator, ObjectMultistack stack) {
		ValueWrapper m = stack.pop(TEMPKEY);
		ValueWrapper n = stack.pop(TEMPKEY);

		switch (operator) {
			case "+":
				n.increment(m.getValue());
				break;
			case "-":
				n.decrement(m.getValue());
				break;
			case "*":
				n.multiply(m.getValue());
				break;
			case "/":
				n.divide(m.getValue());
				break;
			default:
				throw new IllegalArgumentException("Illegal operator.");
		}

		stack.push(TEMPKEY, new ValueWrapper(n.getValue()));
	}

	/**
	 * Method reverses stack content using helper stack. Key for stacks is "temp". In
	 * process of reversing, this method pops all elements from original stack.
	 * @param stack stack to reverse
	 * @return reversed stack
	 */
	protected final ObjectMultistack reverseStack(ObjectMultistack stack) {
		ObjectMultistack reverseStack = new ObjectMultistack();
		reverseStack.push(TEMPKEY, null);
		reverseStack.pop(TEMPKEY);

		while (!stack.isEmpty(TEMPKEY)) {
			ValueWrapper temp = stack.pop(TEMPKEY);
			reverseStack.push(TEMPKEY, temp);
		}

		return reverseStack;
	}

	/**
	 * Method pops all content from given stack and writes it to request context.
	 * @param stack stack
	 * @throws IOException exception while writing to request context
	 */
	protected final void printStack(ObjectMultistack stack) throws IOException {
		StringBuilder builder = new StringBuilder();
		while (!stack.isEmpty(TEMPKEY)) {
			ValueWrapper temp = stack.pop(TEMPKEY);
			builder.append(temp.getValue().toString());
		}
		requestContext.write(builder.toString());
	}

}
