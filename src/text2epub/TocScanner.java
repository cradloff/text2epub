package text2epub;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/** Scannt nach Überschriften für das Inhaltsverzeichnis */
public class TocScanner extends AbstractXmlScanner {
	private boolean scan = false;
	private String id;
	private StringBuilder sb = new StringBuilder();
	private Book book;
	private String outputFilename;

	public TocScanner(Book book, String outputFilename) {
		this.book = book;
		this.outputFilename = outputFilename;
	}

	@Override
	public void startElement(String uri, String localName,
			String qName, Attributes attributes) throws SAXException {
		if ("h1".equals(qName)) {
			scan = true;
			id = attributes.getValue("id");
			sb.setLength(0);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if ("h1".equals(qName)) {
			scan = false;
			String link = outputFilename;
			if (id != null) {
				link += "#" + id;
			}
			book.addTocEntry(new TocEntry(sb.toString().trim(), link));
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (scan) {
			sb.append(ch, start, length);
		}
	}

}
