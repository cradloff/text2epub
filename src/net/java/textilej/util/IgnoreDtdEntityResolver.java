package net.java.textilej.util;

import java.io.IOException;
import java.io.StringReader;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * An entity resolver that resolves all requests for DTD content, thus preventing network access
 * when resolving DTDs.
 *
 */
public class IgnoreDtdEntityResolver implements EntityResolver {
	protected static final IgnoreDtdEntityResolver instance = new IgnoreDtdEntityResolver();
	
	public static IgnoreDtdEntityResolver getInstance() {
		return instance;
	}
	
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		if ((publicId != null && publicId.indexOf("//DTD") != -1) || (systemId != null && systemId.endsWith(".dtd"))) {
			return new InputSource(new StringReader(""));
		}
		return null;
	}
}
