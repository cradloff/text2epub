package text2epub.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** XML-Scanner */
public class XmlScanner {
	private XmlScanner() {}

	/** Scannt ein XML-Fragment */
	public static void scanXmlFragment(String content, DefaultHandler handler) {
		scanXmlFragment(new ByteArrayInputStream(content.getBytes()), handler);
	}

	/** Scannt ein XML-Fragment */
	public static void scanXmlFragment(InputStream in, DefaultHandler handler) {
		// Fragment in Root-Element verpacken
		Enumeration<InputStream> streams = Collections.enumeration(
				Arrays.asList(new InputStream[] {
						new ByteArrayInputStream("<root>".getBytes()),
						in,
						new ByteArrayInputStream("</root>".getBytes()),
				}));

		SequenceInputStream seqStream = new SequenceInputStream(streams);
		scanXml(seqStream, handler);
	}

	/** Scannt eine Datei */
	public static void scanXml(File file, DefaultHandler handler) throws IOException {
		try (FileInputStream fis = new FileInputStream(file)) {
			scanXml(fis, handler);
		}
	}

	/** Scannt einen Input-Stream
	 * @param handler */
	public static void scanXml(InputStream in, DefaultHandler handler) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			SAXParser parser = factory.newSAXParser();
			parser.parse(in, handler);
		} catch (IOException | SAXException | ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}
}
