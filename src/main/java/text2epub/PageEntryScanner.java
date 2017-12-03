package text2epub;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** Scannt nach Seitenzahlen für das Inhaltsverzeichnis */
public class PageEntryScanner extends DefaultHandler {
	private static final String NS_EPUB = "http://www.idpf.org/2007/ops";
	private Book book;
	private String outputFilename;

	public PageEntryScanner(Book book, String outputFilename) {
		this.book = book;
		this.outputFilename = outputFilename;
	}

	@Override
	public void startElement(String uri, String localName,
			String qName, Attributes attributes) throws SAXException {
		// <span epub:type="pagebreak" id="pageX" title="X"/>
		if ("pagebreak".equals(attributes.getValue(NS_EPUB, "type"))) {
			String id = attributes.getValue("id");
			String title = attributes.getValue("title");
			book.addPageEntry(new PageEntry(id, title, outputFilename));
		}
	}

}
