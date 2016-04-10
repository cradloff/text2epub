package text2epub;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/** Sucht nach eingebunden Bildern */
public class ImageScanner extends AbstractXmlScanner {
	private Deque<String> parents = new ArrayDeque<>();
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
		if (
				// img-Tag
				"img".equals(qName)
				// source-Tag in picture-Element
				|| "source".equals(qName) && "picture".equals(parents.peekLast())) {
			String src = attributes.getValue("src");
			if (src != null) {
				images.add(src);
			}
			String srcset = attributes.getValue("srcset");
			if (srcset != null) {
				// Komma-getrennte Liste mit Urls und Breiten- und DPI-Angaben
				// z.B.: srcset="img01.jpg, img02.jpg 200w, img03.jpg 400w 2x"
				String[] t = srcset.split("\\s*,\\s*");
				for (String s : t) {
					String[] u = s.split("\\s+");
					images.add(u[0]);
				}
			}
		}
		// image-Tag in SVG-Grafik
		else if (parents.contains("svg") && "image".equals(qName)) {
			images.add(attributes.getValue("xlink:href"));
		}

		parents.addLast(qName);
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		parents.removeLast();
	}

}
