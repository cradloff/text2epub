package net.java.textilej.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class DefaultXmlStreamWriter implements XmlStreamWriter {

	private PrintWriter out;


	private Map<String,String> prefixToUri = new HashMap<String,String>();
	private Map<String,String> uriToPrefix = new HashMap<String,String>();
	
	private boolean inEmptyElement = false;
	private boolean inStartElement = false;
	
	private Stack<String> elements = new Stack<String>();

	private char xmlHederQuoteChar = '\'';
	
	public DefaultXmlStreamWriter(OutputStream out) throws UnsupportedEncodingException {
		this.out = createUtf8PrintWriter(out);
	}
	
	public DefaultXmlStreamWriter(Writer out) {
		this.out = new PrintWriter(out);
	}
	
	public DefaultXmlStreamWriter(Writer out,char xmlHeaderQuoteChar) {
		this.out = new PrintWriter(out);
		this.xmlHederQuoteChar = xmlHeaderQuoteChar;
	}
	
	protected PrintWriter createUtf8PrintWriter(java.io.OutputStream out) throws UnsupportedEncodingException {
		return new java.io.PrintWriter(new OutputStreamWriter(out, "UTF8"));
	}

	public void close() {
		if (out != null) {
			closeElement();
			flush();
		}
		out = null;
	}

	public void flush() {
		out.flush();
	}


	public String getPrefix(String uri) {
		return uriToPrefix.get(uri);
	}

	public Object getProperty(String name) throws IllegalArgumentException {
		return null;
	}

	public void setDefaultNamespace(String uri) {
		setPrefix("", uri);
	}

	public void setPrefix(String prefix, String uri) {
		prefixToUri.put(prefix,uri);
		uriToPrefix.put(uri,prefix);
	}

	public void writeAttribute(String localName, String value) {
		out.write(' ');
		out.write(localName);
		out.write("=\"");
		if (value != null) {
			attrEncode(value);
		}
		out.write("\"");
	}

	public void writeAttribute(String namespaceURI, String localName, String value) {
		out.write(' ');
		String prefix = uriToPrefix.get(namespaceURI);
		if (prefix != null && prefix.length() > 0) {
			out.write(prefix);
			out.write(':');	
		}
		out.write(localName);
		out.write("=\"");
		if (value != null) {
			attrEncode(value);
		}
		out.write("\"");
	}

	public void writeAttribute(String prefix, String namespaceURI, String localName, String value) {
		out.write(' ');
		if (prefix != null && prefix.length() > 0) {
			out.write(prefix);
			out.write(':');	
		}
		out.write(localName);
		out.write("=\"");
		if (value != null) {
			attrEncode(value);
		}
		out.write("\"");
	}

	private void attrEncode(String value) {
		if (value == null) {
			return;
		}
		printEscaped(out, value, true);
	}
	private void encode(String text) {
		if (text == null) { 
			return;
		}
		printEscaped(out, text, false);
	}
	
	public void writeCData(String data) {
		closeElement();
		out.write("<![CDATA[");
		out.write(data);
		out.write("]]>");
	}

	public void writeCharacters(String text) {
		closeElement();
		encode(text);
	}
	
	public void writeCharactersUnescaped(String text) {
		closeElement();
		out.print(text);
	}
	
	public void writeLiteral(String literal) {
		writeCharactersUnescaped(literal);
	}

	public void writeCharacters(char[] text, int start, int len) {
		closeElement();
		encode(new String(text,start,len));
	}

	public void writeComment(String data) {
		closeElement();
		out.write("<!-- ");
		out.write(data);
		out.write(" -->");
	}

	public void writeDTD(String dtd) {
		out.write(dtd);
	}

	public void writeDefaultNamespace(String namespaceURI) {
		writeAttribute("xmlns",namespaceURI);
	}

	private void closeElement() {
		if (inEmptyElement) {
			out.write("/>");
			inEmptyElement = false;
		} else if (inStartElement) {
			out.write(">");
			inStartElement = false;
		}
	}
	
	public void writeEmptyElement(String localName) {
		closeElement();
		inEmptyElement = true;
		out.write('<');
		out.write(localName);
	}


	public void writeEmptyElement(String namespaceURI, String localName) {
		closeElement();
		inEmptyElement = true;
		String prefix = uriToPrefix.get(namespaceURI);
		out.write('<');
		if (prefix != null && prefix.length() > 0) {
			out.write(prefix);
			out.write(':');
		}
		out.write(localName);
	}

	public void writeEmptyElement(String prefix, String localName, String namespaceURI) {
		closeElement();
		inEmptyElement = true;
		out.write('<');
		if (prefix != null && prefix.length() > 0) {
			out.write(prefix);
			out.write(':');
		}
		out.write(localName);
	}

	public void writeEndDocument() {
		if (!elements.isEmpty()) { 
			throw new IllegalStateException(elements.size()+" elements not closed");
		}
	}

	public void writeEndElement() {
		closeElement();
		if (elements.isEmpty()) {
			throw new IllegalStateException();
		}
		String name = elements.pop();
		out.write('<');
		out.write('/');
		out.write(name);
		out.write('>');
	}

	public void writeEntityRef(String name) {
		closeElement();
		out.write('&');
		out.write(name);
		out.write(';');
	}

	public void writeNamespace(String prefix, String namespaceURI) {
		if (prefix == null || prefix.length() == 0) {
			writeAttribute("xmlns",namespaceURI);
		} else {
			writeAttribute("xmlns:"+prefix,namespaceURI);
		}
	}

	public void writeProcessingInstruction(String target) {
		closeElement();
	}

	public void writeProcessingInstruction(String target, String data) {
		closeElement();
		
	}

	public void writeStartDocument() {
		out.write(processXmlHeader("<?xml version='1.0' ?>"));
	}

	public void writeStartDocument(String version) {
		out.write(processXmlHeader("<?xml version='"+version+"' ?>"));
	}

	public void writeStartDocument(String encoding, String version) {
		out.write(processXmlHeader("<?xml version='"+version+"' encoding='"+encoding+"' ?>"));
	}

	public void writeStartElement(String localName) {
		closeElement();
		inStartElement = true;
		elements.push(localName);
		out.write('<');
		out.write(localName);
	}

	public void writeStartElement(String namespaceURI, String localName) {
		closeElement();
		inStartElement = true;
		String prefix = uriToPrefix.get(namespaceURI);
		out.write('<');
		if (prefix != null && prefix.length() > 0) {
			out.write(prefix);
			out.write(':');
			elements.push(prefix+':'+localName);
		} else {
			elements.push(localName);
		}
		out.write(localName);		
	}

	public void writeStartElement(String prefix, String localName, String namespaceURI) {
		closeElement();
		inStartElement = true;
		elements.push(localName);
		out.write('<');
		if (prefix != null && prefix.length() > 0) {
			out.write(prefix);
			out.write(':');
		}
		out.write(localName);
	}

	public char getXmlHederQuoteChar() {
		return xmlHederQuoteChar;
	}

	public void setXmlHederQuoteChar(char xmlHederQuoteChar) {
		this.xmlHederQuoteChar = xmlHederQuoteChar;
	}

	private String processXmlHeader(String header) {
		return xmlHederQuoteChar == '\''?header:header.replace('\'', xmlHederQuoteChar);
	}

	private static void printEscaped(PrintWriter writer, CharSequence s,boolean attribute) {
		int length = s.length();

		try {
			for (int x = 0; x < length; ++x) {
				char ch = s.charAt(x);
				printEscaped(writer, ch,attribute);
			}
		} catch (IOException ioe) {
			throw new IllegalStateException();
		}
	}
	

	/**
	 * Print an XML character in its escaped form.
	 * 
	 * @param writer
	 *            The writer to which the character should be printed.
	 * @param ch
	 *            the character to print.
	 * 
	 * @throws IOException
	 */
	private static void printEscaped(PrintWriter writer, int ch,boolean attribute) throws IOException {

		String ref = getEntityRef(ch,attribute);
		if (ref != null) {
			writer.write('&');
			writer.write(ref);
			writer.write(';');
		} else if (ch == '\r' || ch == 0x0085 || ch == 0x2028) {
			printHex(writer, ch);
		} else if ((ch >= ' ' && ch != 160 && isUtf8Printable((char) ch) && isXML11ValidLiteral(ch)) || ch == '\t' || ch == '\n' || ch == '\r') {
			writer.write((char) ch);
		} else {
			printHex(writer, ch);
		}
	}

	/**
	 * Escapes chars
	 */
	final static void printHex(PrintWriter writer, int ch) throws IOException {
		writer.write("&#x");
		writer.write(Integer.toHexString(ch));
		writer.write(';');
	}
	
	
	protected static String getEntityRef(int ch,boolean attribute) {
		// Encode special XML characters into the equivalent character
		// references.
		// These five are defined by default for all XML documents.
		switch (ch) {
		case '<':
			return "lt";
			
			// no need to escape '>'!!
//		case '>':
//			return "gt";
		case '"':
			if (attribute) {
				return "quot";
			}
			break;
		case '&':
			return "amp";

		// WARN: there is no need to encode apostrophe, and doing so has an
		// adverse
		// effect on XHTML documents containing javascript with some browsers.
		// case '\'':
		// return "apos";
		}
		return null;
	}

	protected static boolean isUtf8Printable(char ch) {
		// fall-back method here.
		if ((ch >= ' ' && ch <= 0x10FFFF && ch != 0xF7) || ch == '\n' || ch == '\r' || ch == '\t') {
			// If the character is not printable, print as character reference.
			// Non printables are below ASCII space but not tab or line
			// terminator, ASCII delete, or above a certain Unicode threshold.
			return true;
		}

		return false;
	}

	
	
	private static final byte XML11CHARS[] = new byte[1 << 16];
	public static final int MASK_XML11_VALID = 0x01;
	public static final int MASK_XML11_CONTROL = 0x10;

	static {

		// Initializing the Character Flag Array
		// Code generated by: XML11CharGenerator.

		Arrays.fill(XML11CHARS, 1, 9, (byte) 17); // Fill 8 of value (byte) 17
		XML11CHARS[9] = 35;
		XML11CHARS[10] = 3;
		Arrays.fill(XML11CHARS, 11, 13, (byte) 17); // Fill 2 of value (byte) 17
		XML11CHARS[13] = 3;
		Arrays.fill(XML11CHARS, 14, 32, (byte) 17); // Fill 18 of value (byte)
													// 17
		XML11CHARS[32] = 35;
		Arrays.fill(XML11CHARS, 33, 38, (byte) 33); // Fill 5 of value (byte) 33
		XML11CHARS[38] = 1;
		Arrays.fill(XML11CHARS, 39, 45, (byte) 33); // Fill 6 of value (byte) 33
		Arrays.fill(XML11CHARS, 45, 47, (byte) -87); // Fill 2 of value
														// (byte) -87
		XML11CHARS[47] = 33;
		Arrays.fill(XML11CHARS, 48, 58, (byte) -87); // Fill 10 of value
														// (byte) -87
		XML11CHARS[58] = 45;
		XML11CHARS[59] = 33;
		XML11CHARS[60] = 1;
		Arrays.fill(XML11CHARS, 61, 65, (byte) 33); // Fill 4 of value (byte) 33
		Arrays.fill(XML11CHARS, 65, 91, (byte) -19); // Fill 26 of value
														// (byte) -19
		Arrays.fill(XML11CHARS, 91, 93, (byte) 33); // Fill 2 of value (byte) 33
		XML11CHARS[93] = 1;
		XML11CHARS[94] = 33;
		XML11CHARS[95] = -19;
		XML11CHARS[96] = 33;
		Arrays.fill(XML11CHARS, 97, 123, (byte) -19); // Fill 26 of value
														// (byte) -19
		Arrays.fill(XML11CHARS, 123, 127, (byte) 33); // Fill 4 of value
														// (byte) 33
		Arrays.fill(XML11CHARS, 127, 133, (byte) 17); // Fill 6 of value
														// (byte) 17
		XML11CHARS[133] = 35;
		Arrays.fill(XML11CHARS, 134, 160, (byte) 17); // Fill 26 of value
														// (byte) 17
		Arrays.fill(XML11CHARS, 160, 183, (byte) 33); // Fill 23 of value
														// (byte) 33
		XML11CHARS[183] = -87;
		Arrays.fill(XML11CHARS, 184, 192, (byte) 33); // Fill 8 of value
														// (byte) 33
		Arrays.fill(XML11CHARS, 192, 215, (byte) -19); // Fill 23 of value
														// (byte) -19
		XML11CHARS[215] = 33;
		Arrays.fill(XML11CHARS, 216, 247, (byte) -19); // Fill 31 of value
														// (byte) -19
		XML11CHARS[247] = 33;
		Arrays.fill(XML11CHARS, 248, 768, (byte) -19); // Fill 520 of value
														// (byte) -19
		Arrays.fill(XML11CHARS, 768, 880, (byte) -87); // Fill 112 of value
														// (byte) -87
		Arrays.fill(XML11CHARS, 880, 894, (byte) -19); // Fill 14 of value
														// (byte) -19
		XML11CHARS[894] = 33;
		Arrays.fill(XML11CHARS, 895, 8192, (byte) -19); // Fill 7297 of value
														// (byte) -19
		Arrays.fill(XML11CHARS, 8192, 8204, (byte) 33); // Fill 12 of value
														// (byte) 33
		Arrays.fill(XML11CHARS, 8204, 8206, (byte) -19); // Fill 2 of value
															// (byte) -19
		Arrays.fill(XML11CHARS, 8206, 8232, (byte) 33); // Fill 26 of value
														// (byte) 33
		XML11CHARS[8232] = 35;
		Arrays.fill(XML11CHARS, 8233, 8255, (byte) 33); // Fill 22 of value
														// (byte) 33
		Arrays.fill(XML11CHARS, 8255, 8257, (byte) -87); // Fill 2 of value
															// (byte) -87
		Arrays.fill(XML11CHARS, 8257, 8304, (byte) 33); // Fill 47 of value
														// (byte) 33
		Arrays.fill(XML11CHARS, 8304, 8592, (byte) -19); // Fill 288 of value
															// (byte) -19
		Arrays.fill(XML11CHARS, 8592, 11264, (byte) 33); // Fill 2672 of
															// value (byte) 33
		Arrays.fill(XML11CHARS, 11264, 12272, (byte) -19); // Fill 1008 of
															// value (byte) -19
		Arrays.fill(XML11CHARS, 12272, 12289, (byte) 33); // Fill 17 of value
															// (byte) 33
		Arrays.fill(XML11CHARS, 12289, 55296, (byte) -19); // Fill 43007 of
															// value (byte) -19
		Arrays.fill(XML11CHARS, 57344, 63744, (byte) 33); // Fill 6400 of
															// value (byte) 33
		Arrays.fill(XML11CHARS, 63744, 64976, (byte) -19); // Fill 1232 of
															// value (byte) -19
		Arrays.fill(XML11CHARS, 64976, 65008, (byte) 33); // Fill 32 of value
															// (byte) 33
		Arrays.fill(XML11CHARS, 65008, 65534, (byte) -19); // Fill 526 of value
															// (byte) -19

	} // <clinit>()
	public static boolean isXML11ValidLiteral(int c) {
		return ((c < 0x10000 && ((XML11CHARS[c] & MASK_XML11_VALID) != 0 && (XML11CHARS[c] & MASK_XML11_CONTROL) == 0)) || (0x10000 <= c && c <= 0x10FFFF));
	}
	
}