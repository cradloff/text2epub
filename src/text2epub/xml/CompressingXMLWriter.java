package text2epub.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

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

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		flushElement();
		startElement = true;
		this.uri = uri;
		this.localName = localName;
		this.qName = qName;
		this.atts = atts;
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

	private void flushElement() throws SAXException {
		if (startElement) {
			super.startElement(uri, localName, qName, atts);
			startElement = false;
		}
	}
}
