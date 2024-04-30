package text2epub;

import java.util.*;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/** Sucht nach eingebunden Bildern */
public class ImageScanner extends XMLFilterImpl {
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
		AttributesImpl modifiedAttributes = new AttributesImpl(attributes);
		if (
				// img-Tag
				"img".equals(qName)
				// source-Tag in picture-Element
				|| "source".equals(qName) && "picture".equals(parents.peekLast())) {
			String src = attributes.getValue("src");
			if (src != null) {
				String file = addEntry(src);
				modifyAttribute(modifiedAttributes, "src", file);
			}
			String srcset = attributes.getValue("srcset");
			if (srcset != null) {
				// Komma-getrennte Liste mit Urls und Breiten- und DPI-Angaben
				// z.B.: srcset="img01.jpg, img02.jpg 200w, img03.jpg 400w 2x"
				List<String> t = StringUtils.splitCSV(srcset);
				List<String> t2 = new ArrayList<>();
				for (String s : t) {
					String[] u = s.split("\\s+");
					String file = addEntry(u[0]);
					u[0] = file;
					t2.add(String.join(" ", u));
				}
				modifyAttribute(modifiedAttributes, "srcset", String.join(", ", t2));
			}
		}
		// image-Tag in SVG-Grafik
		else if (parents.contains("svg") && "image".equals(qName)) {
			String link = attributes.getValue("xlink:href");
			// eingebettete Grafiken ignorieren
			if (! link.startsWith("data:")) {
				String file = addEntry(link);
				modifyAttribute(modifiedAttributes, "xlink:href", file);
			}
		}

		parents.addLast(qName);
		super.startElement(uri, localName, qName, modifiedAttributes);
	}

	private String addEntry(String src) {
		String id = String.format("img-%02d", images.size());
		FileEntry entry = new FileEntry(src, IOUtils.normalize(src), MimeTypes.getMimeType(src), id);
		images.add(entry);
		return entry.getFilename();
	}
	
	private void modifyAttribute(AttributesImpl modifiedAttributes, String name, String value) {
		modifiedAttributes.removeAttribute(modifiedAttributes.getIndex(name));
		modifiedAttributes.addAttribute("", name, "", "string", value);
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		parents.removeLast();
		super.endElement(uri, localName, qName);
	}

}
