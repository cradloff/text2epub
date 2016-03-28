package text2epub;

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

/** Basisklasse für XML-Scanner */
public class AbstractXmlScanner extends DefaultHandler {
	/** Scannt ein XML-Fragment */
	public void scanXmlFragment(String content) {
		scanXmlFragment(new ByteArrayInputStream(content.getBytes()));
	}

	/** Scannt ein XML-Fragment */
	public void scanXmlFragment(InputStream in) {
		// Fragment in Root-Element verpacken
		Enumeration<InputStream> streams = Collections.enumeration(
				Arrays.asList(new InputStream[] {
						new ByteArrayInputStream("<root>".getBytes()),
						in,
						new ByteArrayInputStream("</root>".getBytes()),
				}));

		SequenceInputStream seqStream = new SequenceInputStream(streams);
		scanXml(seqStream);
	}

	/** Scannt eine Datei */
	public void scanXml(File file) throws IOException {
		try (FileInputStream fis = new FileInputStream(file)) {
			scanXml(fis);
		}
	}

	/** Scannt einen Input-Stream */
	public void scanXml(InputStream in) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			SAXParser parser = factory.newSAXParser();
			parser.parse(in, this);
		} catch (IOException | SAXException | ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

}
