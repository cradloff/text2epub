package text2epub;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** Sucht nach eingebunden Bildern */
public class ImageScanner extends DefaultHandler {
	private Deque<String> parents = new ArrayDeque<>();
	private Set<FileEntry> images;

	/**
	 * Konstruktor.
	 * @param images Set mit den Bildern
	 */
	public ImageScanner(Set<FileEntry> images) {
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
				addEntry(src);
			}
			String srcset = attributes.getValue("srcset");
			if (srcset != null) {
				// Komma-getrennte Liste mit Urls und Breiten- und DPI-Angaben
				// z.B.: srcset="img01.jpg, img02.jpg 200w, img03.jpg 400w 2x"
				String[] t = srcset.split("\\s*,\\s*");
				for (String s : t) {
					String[] u = s.split("\\s+");
					addEntry(u[0]);
				}
			}
		}
		// image-Tag in SVG-Grafik
		else if (parents.contains("svg") && "image".equals(qName)) {
			addEntry(attributes.getValue("xlink:href"));
		}

		parents.addLast(qName);
	}

	private void addEntry(String src) {
		String id = String.format("img-%02d", images.size());
		FileEntry entry = new FileEntry(src, MimeTypes.getMimeType(src), id);
		images.add(entry);
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		parents.removeLast();
	}

}
