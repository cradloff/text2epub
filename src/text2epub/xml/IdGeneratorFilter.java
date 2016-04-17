package text2epub.xml;

import java.util.Collection;

import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.XMLFilterImpl;

public class IdGeneratorFilter extends XMLFilterImpl {
	private final Collection<String> elements;

	public IdGeneratorFilter(Collection<String> elements) {
		this.elements = elements;
	}

	@Override
	public void setContentHandler(ContentHandler handler) {
		super.setContentHandler(new IdGeneratorHandler(handler, elements));
	}

}
