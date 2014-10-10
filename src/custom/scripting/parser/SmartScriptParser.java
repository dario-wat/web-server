package custom.scripting.parser;

import custom.collections.ArrayBackedIndexedCollection;
import custom.collections.ObjectStack;
import custom.scripting.nodes.DocumentNode;
import custom.scripting.nodes.EchoNode;
import custom.scripting.nodes.ForLoopNode;
import custom.scripting.nodes.Node;
import custom.scripting.nodes.TextNode;
import custom.scripting.tokens.Token;
import custom.scripting.tokens.TokenConstantDouble;
import custom.scripting.tokens.TokenConstantInteger;
import custom.scripting.tokens.TokenFunction;
import custom.scripting.tokens.TokenOperator;
import custom.scripting.tokens.TokenString;
import custom.scripting.tokens.TokenVariable;

/**
 * Služi za parsiranje dokumenta (stringa) te sastavlja stablo nodova.
 * @author Dario
 */
public class SmartScriptParser {

	/**
	 * Sadrži dokument koji se parsira. Element na dnu stoga.
	 */
	private DocumentNode docNode;

	/**
	 * Sadrži string koji se parsira.
	 */
	private String docBody;

	/**
	 * Stoga za lakšu realizaciju stabla.
	 */
	private ObjectStack stack;

	/**
	 * Kolekcija za lakšu realizaciju EchoNode-a odnosno
	 * za spremanje njegovih tokena.
	 */
	private ArrayBackedIndexedCollection coll;

	//iducih 8 su stanja osnovnog automata

	/**
	 * Pocetno stanje automata.
	 */
	private static final int POC_STANJE = 0;

	/**
	 * Stanje automata u tekstualnom dijelu.
	 */
	private static final int TEXT_STANJE = 1;

	/**
	 * Stanje nakon sto se otvori uglata zagrada.
	 */
	private static final int OPEN_B_STANJE = 2;

	/**
	 * Stanje nakon znaka $ pri otvaranju tagova.
	 */
	private static final int OPEN_D_STANJE = 3;

	/**
	 * Stanje automata u dijelu dok čita tag.
	 */
	private static final int TAG_STANJE = 4;

	/**
	 * Stanje u koje ulazi pri čitanju prvog znaka $ unutar tagova.
	 * Služi za pronalaženje kraja taga.
	 */
	private static final int CLOSE_D_STANJE = 5;

	/**
	 * Stanje nakon ulaska znaka ] koji se pojavljuje isključivo
	 * nakon znaka $.
	 */
	private static final int CLOSE_B_STANJE = 6;

	/**
	 * Escape stanje u koje ulazi nakon čitanja znaka \ u tekstualnom dijelu.
	 */
	private static final int ESC_STANJE = 7;

	/**
	 * Služi za lakše sastavljanje stringova iz znakova s automata.
	 */
	private StringBuilder build;

	/**
	 * Prima string koji treba parsirat te pokreće parser. Stvara stog
	 * koji služi za realizaciju stabla. Parser metoda dalje
	 * dodjeljuje zadatke.
	 * @param docBody string koji parsira
	 */
	public SmartScriptParser(String docBody) {
		super();
		this.docBody = docBody;
		this.stack = new ObjectStack();
		parser();
	}

	/**
	 * Getter za DocumentNode.
	 * @return vraća DocumentNode koji se parsira
	 */
	public DocumentNode getDocumentNode() {
		return docNode;
	}

	/**
	 * Obrađuje tekst i stavlja na stog novi node.
	 */
	private void doText() throws SmartScriptParserException {
		if (stack.isEmpty()) {
			throw new SmartScriptParserException("Pokusaj skidanja s praznog stoga");
		}

		//mijenja svaki \[ sa [
		String text = build.toString();
		text = text.replace("\\[", "[");

		//pravim novi node, i dodajem roditelju djete
		TextNode noviNode = new TextNode(text);
		Node roditelj = (Node) stack.peek();
		roditelj.addChildNode(noviNode);
	}

	/**
	 * Određuje o kojoj se vrsti taga radi te ih šalje na daljnju obradu.
	 */
	private void doTag() throws SmartScriptParserException {
		String tag = build.toString();
		tag = tag.trim();				//trim :D

		//određuje tag na primitivan način
		if (tag.charAt(0) == '=') {
			doEchoTag();
		} else if (tag.equalsIgnoreCase("END")) {
			doEndTag();
		} else if (tag.length() >= 4 && tag.substring(0, 4).compareToIgnoreCase("FOR ") == 0) {
			doForTag();
		} else {
			throw new SmartScriptParserException("Tag je neispravan (nije FOR, END ni ECHO");
		}
	}

	/**
	 * Provjerava je li dani string ispravno ime (varijable ili funkcije).
	 * Ispravno ime počinje sa slovom te se nastavlja sa 0 ili više
	 * brojki, slova ili podcrtom.
	 * @param ime string koji provjerava
	 * @return true ako je ime ispravno, inače false
	 */
	private static boolean ispravnoIme(String ime) {
		if (ime.length() < 1) {
			return false;
		}

		//prvi znak
		char znak = ime.charAt(0);
		if (!(Character.isLetter(znak))) {
			return false;
		}

		//ostali znakovi
		for (int i = 1; i < ime.length(); i++) {
			if (!(Character.isDigit(znak) || Character.isLetter(znak) || znak == '_')) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Provjerava je li dani string integer. Provjerava može li se
	 * string pretvoriti u integer.
	 * @param izraz string za provjeru
	 * @return true ako može, inače false
	 */
	private static boolean isInteger(String izraz) {
		try {
			Integer.parseInt(izraz);
		} catch (NumberFormatException e) {
			return false;
		}

		return true;
	}

	/**
	 * provjerava je li dani string double. Provjerava može li se
	 * string pretvoriti u integer.
	 * @param izraz string za provjeru
	 * @return true ako može, inače false
	 */
	private static boolean isDouble(String izraz) {
		try {
			Double.parseDouble(izraz);
		} catch (NumberFormatException e) {
			return false;
		}

		return true;
	}

	/**
	 * Uređuje stringove unutar tagova. Uređuje svaki string koji
	 * se nalazi unutar znakova " " unutar tagova. Vrlo primitivno
	 * mijenja sve escape-ove.
	 * @param str string koji uređuje
	 * @return vraća uređeni string
	 */
	private static String obradiString(String str) {
		String string = str;
		string = string.replace("\\\\", "\\");		//mijenja \\ sa \
		string = string.replace("\\\"", "\"");		//mijenja \" sa "
		string = string.replace("\\n", "\n");			//mijenja \n sa line feed
		string = string.replace("\\t", "\t");			//mijenja \t sa tabom
		string = string.replace("\\r", "\r");			//mijenja \r sa carriage return
		return string;
	}

	/**
	 * Za dobiveni string provjerava njegov tip te vraća odgovarajući token.
	 * Provjerava je li string jednak jednom od sljedećih tipova:
	 * integer, double, function, operator, string, variable. Odmah
	 * poziva metodu za provjeru imena varijabli i funkcija. Stvara iz tipa
	 * novi token te ga vraća. Ukoliko postoji neka grešla u bilo kojem tokenu
	 * ili se primi nepostojeći token, baca iznimku.
	 * @param token string za provjeru
	 * @return vraća stvoreni token
	 */
	private Token odrediToken(String token) throws SmartScriptParserException {
		if (isInteger(token)) {					//integer
			return new TokenConstantInteger(Integer.parseInt(token));
		} else if (isDouble(token)) {			//double
			return new TokenConstantDouble(Double.parseDouble(token));
		} else if (token.charAt(0) == '@') {				//function
			if (ispravnoIme(token.substring(1))) {			//provjera imena
				return new TokenFunction(token.substring(1));
			}
		} else if (token.equals("+") || token.equals("*") || 		//operator
				token.equals("-") || token.equals("/")) {
			return new TokenOperator(token);
		} else if (token.charAt(0) == '"' && token.charAt(token.length() - 1) == '"') {
			String tmp = token.substring(1, token.length() - 1);	//string
			return new TokenString(obradiString(tmp));				//obrada stringa
		} else if (Character.isLetter(token.charAt(0))) {			//varijabla
			if (ispravnoIme(token)) {								//provjera imena
				return new TokenVariable(token);
			}
		}

		/*
		 * ukoliko niti jedan token uvjet nije zadovoljen
		 * !!!izvedi ovo pametnije ako stigneš!!!
		 */
		throw new SmartScriptParserException("Pogrešan token (mogucnost neispravnog imena)");
	}

	/**
	 * Obrađuje FOR tag. Provjerava ima li tag 3 ili 4 argumenta. Ako nema,
	 * baca exception. Ukoliko nema četvrtog argumenta, stavlja null na
	 * njegovo mjesto. Dodaje sebe svome roditelju te se stavlja na stog
	 * (jer i sam FOR postaje roditelj).
	 */
	private void doForTag() throws SmartScriptParserException {
		final int PRVIARG = 0;
		final int DRUGIARG = 1;
		final int TRECIARG = 2;
		final int ZADNJIARG = 3;
		final int BRARG = 4;

		String tag = build.toString();
		tag = tag.trim();
		tag = tag.substring(4);			//izbacujem FOR
		tag = tag.trim();				//trim :D

		String[] skup = new String[BRARG];
		StringBuilder niz = new StringBuilder();

		//dodajem prva 3 argumenta u polje
		int index = 0;
		boolean firstSpace = false;
		for (int i = 0; i < tag.length(); i++) {
			char znak = tag.charAt(i);

			if (znak != ' ') {
				niz.append(znak);
				firstSpace = false;
			} else {
				if (!firstSpace) {
					skup[index] = niz.toString();
					index++;
					niz = new StringBuilder();
					firstSpace = true;
				}
			}

			if (index > 3) {
				throw new SmartScriptParserException("FOR tag ne smije imat vise od 4 tokena");
			}
		}
		skup[index] = niz.toString();		//dodajem zaostali, zadnji (treci ili cetvrti

		if (index < 2) {
			throw new SmartScriptParserException("FOR tag ima premalo tokena");
		}

		//pretvaram stringove u tokene
		Token[] forLoopTokens = new Token[BRARG];
		for (int i = 0; i < 3; i++) {
			forLoopTokens[i] = odrediToken(skup[i]);
		}

		//provjeravam cetvrti argument
		ForLoopNode noviNode;
		if (skup[3] == null) {
			noviNode = new ForLoopNode(
					(TokenVariable) forLoopTokens[PRVIARG],
					forLoopTokens[DRUGIARG],
					forLoopTokens[TRECIARG],
					null);
		} else {
			forLoopTokens[ZADNJIARG] = odrediToken(skup[ZADNJIARG]);
			noviNode = new ForLoopNode(
					(TokenVariable) forLoopTokens[PRVIARG],
					forLoopTokens[DRUGIARG],
					forLoopTokens[TRECIARG],
					odrediToken(skup[ZADNJIARG]));
		}

		//djete roditelju pa na stog
		Node roditelj = (Node) stack.peek();
		roditelj.addChildNode(noviNode);
		stack.push(noviNode);
	}

	/**
	 * Obrađuje ECHO tagove (tagove koji započinju sa =). Dodaje se roditelju.
	 */
	private void doEchoTag() {
		coll = new ArrayBackedIndexedCollection();		//za laksu pohranu tokena
		String text = build.toString();
		text = text.trim();					//trim :D
		text = text.substring(1);			//izbacujem =
		text = text.trim();					//trim :D

		StringBuilder niz = new StringBuilder();

		//redom trpam stringove, znakove stogod u kolekciju
		boolean firstSpace = false;
		boolean inString = false;
		for (int i = 0; i < text.length(); i++) {
			char znak = text.charAt(i);
			if (znak == '"') {
				inString = !inString;
			}
			if (inString) {
				niz.append(znak);
				continue;
			}

			if (znak != ' ' && znak != '\n' && znak != '\r' && znak != '\t') {
				niz.append(znak);
				firstSpace = false;
			} else {
				if (!firstSpace) {
					coll.add(niz.toString());
					niz = new StringBuilder();
					firstSpace = true;
				}
			}
		}
		if (inString) {
			throw new SmartScriptParserException("Nisam izasao iz stringa u echo tagu.");
		}
		coll.add(niz.toString());		//zadnji zaostali

		//pretvaram u tokene
		Token[] tokens = new Token[coll.size()];
		for (int i = 0; i < coll.size(); i++) {
			tokens[i] = odrediToken((String) coll.get(i));
		}

		//djete roditelju
		Node roditelj = (Node) stack.peek();
		EchoNode noviNode = new EchoNode(tokens);
		roditelj.addChildNode(noviNode);
	}

	/**
	 * Obrađuje END tagove tako da skine jedan element sa stoga.
	 */
	private void doEndTag() throws SmartScriptParserException {
		if (stack.isEmpty()) {
			throw new SmartScriptParserException("Stog je prazan, ne mogu END izvršit");
		}

		stack.pop();
	}

	/**
	 * Parser izveden pomoću Mealyjevog automata s 8 stanja te
	 * te devetim nepostojećim pogrešnim stanjem.
	 */
	private void parser() {
		docNode = new DocumentNode(docBody);
		stack.push(docNode);					//roditelj svih roditelja
		char[] niz = docBody.toCharArray();			//za automat

		build = new StringBuilder();

		int stanje = POC_STANJE;

		//znak po znak automat
		for (int i = 0; i < niz.length; i++) {
			char znak = niz[i];

			switch (stanje) {
				case POC_STANJE:
					if (znak == '[') {
						stanje = OPEN_B_STANJE;
					} else if (znak == '\\') {
						build.append(znak);
						stanje = ESC_STANJE;
					} else {
						build.append(znak);
						stanje = TEXT_STANJE;
					}
					break;
				case TEXT_STANJE:
					if (znak == '[') {
						stanje = OPEN_B_STANJE;
					} else if (znak == '\\') {
						build.append(znak);
						stanje = ESC_STANJE;
					} else {
						build.append(znak);
						stanje = TEXT_STANJE;
					}
					if (stanje == OPEN_B_STANJE) {
						doText();				//tu ide obrada teksta
						build = new StringBuilder();
					}
					break;
				case OPEN_B_STANJE:
					if (znak == ' ') {
						stanje = OPEN_B_STANJE;
					} else if (znak == '$') {
						stanje = OPEN_D_STANJE;
					} else {
						throw new SmartScriptParserException("Iza otvaranja taga [ nije dosao $");
					}
					break;
				case OPEN_D_STANJE:
					if (znak != '$') {
						build.append(znak);
						stanje = TAG_STANJE;
					} else {
						throw new SmartScriptParserException("Između tagova nema ništa");
					}
					break;
				case TAG_STANJE:
					if (znak == '$') {
						stanje = CLOSE_D_STANJE;
					} else {
						build.append(znak);
						stanje = TAG_STANJE;
					}
					if (stanje == CLOSE_D_STANJE) {
						doTag();				//tu ide obrada taga
						build = new StringBuilder();
					}
					break;
				case CLOSE_D_STANJE:
					if (znak == ' ') {
						stanje = CLOSE_D_STANJE;
					} else if (znak == ']') {
						stanje = CLOSE_B_STANJE;
					} else {
						throw new SmartScriptParserException("Između $ i ] je upao neki znak");
					}
					break;
				case CLOSE_B_STANJE:
					if (znak == '[') {
						stanje = OPEN_B_STANJE;
					} else {
						stanje = TEXT_STANJE;
						build.append(znak);
					}
					break;
				case ESC_STANJE:
					build.append(znak);
					stanje = TEXT_STANJE;
					break;
				default:
					throw new SmartScriptParserException("Nepostojeće stanje automata");
			}
		}

		//za kraj ako sam ostao u tekstu obrađujem
		if (stanje == TEXT_STANJE) {
			doText();
		}

		/*
		 * ako sam u jednom od ovih stanja ostao to znaci
		 * da tag nije zavrsen do kraja
		 */
		if (stanje == OPEN_B_STANJE || stanje == OPEN_D_STANJE || stanje == CLOSE_D_STANJE
				|| stanje == TAG_STANJE) {
			throw new SmartScriptParserException("Nedovršen tag na kraju");
		}

		if (stack.isEmpty()) {
			throw new SmartScriptParserException("Stog je prazan, ne mogu document skinut");
		}
		stack.pop();
	}
}
