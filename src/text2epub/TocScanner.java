package text2epub;

import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** Scannt nach Überschriften für das Inhaltsverzeichnis */
public class TocScanner extends DefaultHandler {
	private final List<String> headings;
	private boolean scan = false;
	private String id;
	private StringBuilder sb = new StringBuilder();
	private Book book;
	private String outputFilename;

	public TocScanner(Book book, String outputFilename, List<String> headings) {
		this.book = book;
		this.outputFilename = outputFilename;
		this.headings = headings;
	}

	@Override
	public void startElement(String uri, String localName,
			String qName, Attributes attributes) throws SAXException {
		if (headings.contains(qName)) {
			scan = true;
			id = attributes.getValue("id");
			sb.setLength(0);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (headings.contains(qName)) {
			scan = false;
			String link = outputFilename;
			if (id != null) {
				link += "#" + id;
			}
			book.addTocEntry(new TocEntry(qName, sb.toString().trim(), link));
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
