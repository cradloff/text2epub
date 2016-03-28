package text2epub;

import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/** Sucht nach eingebunden Bildern */
public class ImageScanner extends AbstractXmlScanner {
	private Set<String> images;

	/**
	 * Konstruktor.
	 * @param images Set mit den Bildern
	 */
	public ImageScanner(Set<String> images) {
		this.images = images;
	}

	@Override
	public void startElement(String uri, String localName,
			String qName, Attributes attributes) throws SAXException {
		if ("img".equals(qName)) {
			images.add(attributes.getValue("src"));
		}
	}

}
