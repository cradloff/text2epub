package text2epub.xml;

import java.io.IOException;
import java.io.Writer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Fasst ein Start-Element, das direkt von einem End-Element gefolgt wird,
 * zu einem leeren Element zusammen.
 * @author Claus Radloff
 */
public class CompressingXMLWriter extends XMLWriter {
	private String uri;
	private String localName;
	private String qName;
	private Attributes atts;
	private boolean startElement = false;
	private Writer output;

	@Override
	public void setOutput(Writer writer) {
		super.setOutput(writer);
		this.output = writer;
	}

	@Override
	public void startDocument() throws SAXException {
		// XML-Deklaration und DOCTYPE ausgeben
		try {
			output.write("<?xml version='1.0'?>\n");
			output.write("<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.1//EN' 'http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd'>\n");
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		flushElement();
		startElement = true;
		this.uri = uri;
		this.localName = localName;
		this.qName = qName;
		this.atts = new AttributesImpl(atts);
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (startElement) {
			super.emptyElement(uri, localName, qName, atts);
			startElement = false;
		} else {
			super.endElement(uri, localName, qName);
		}
	}

	@Override
	public void characters(char[] ch, int start, int len) throws SAXException {
		flushElement();
		super.characters(ch, start, len);
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		flushElement();
		super.ignorableWhitespace(ch, start, length);
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		try {
			output.write('&');
			output.write(name);
			output.write(';');
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	private void flushElement() throws SAXException {
		if (startElement) {
			super.startElement(uri, localName, qName, atts);
			startElement = false;
		}
	}
}
