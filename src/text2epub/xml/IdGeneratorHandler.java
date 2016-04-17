package text2epub.xml;

import java.util.Collection;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/** Versorgt Header-Elemente mit einer eindeutigen ID */
public class IdGeneratorHandler extends FilterContentHandler {
	private final Collection<String> elements;
	private int count;

	public IdGeneratorHandler(ContentHandler parent, Collection<String> elements) {
		super(parent);
		this.elements = elements;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		if (elements.contains(qName)) {
			AttributesImpl newAtts = new AttributesImpl(atts);
			String id = String.format("%s-%02d", qName, ++count);
			newAtts.addAttribute("", "id", "id", "ID", id);
			super.startElement(uri, localName, qName, newAtts);
		} else {
			super.startElement(uri, localName, qName, atts);
		}
	}

}
