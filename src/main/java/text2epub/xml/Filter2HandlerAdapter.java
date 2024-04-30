package text2epub.xml;

import java.io.IOException;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLFilterImpl;

public class Filter2HandlerAdapter extends DefaultHandler {
	private XMLFilterImpl filter;

	public Filter2HandlerAdapter(XMLFilterImpl filter) {
		super();
		this.filter = filter;
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		filter.characters(ch, start, length);
	}

	public void endDocument() throws SAXException {
		filter.endDocument();
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		filter.endElement(uri, localName, qName);
	}

	public void endPrefixMapping(String prefix) throws SAXException {
		filter.endPrefixMapping(prefix);
	}

	public boolean equals(Object obj) {
		return filter.equals(obj);
	}

	public void error(SAXParseException e) throws SAXException {
		filter.error(e);
	}

	public void fatalError(SAXParseException e) throws SAXException {
		filter.fatalError(e);
	}

	public ContentHandler getContentHandler() {
		return filter.getContentHandler();
	}

	public DTDHandler getDTDHandler() {
		return filter.getDTDHandler();
	}

	public EntityResolver getEntityResolver() {
		return filter.getEntityResolver();
	}

	public ErrorHandler getErrorHandler() {
		return filter.getErrorHandler();
	}

	public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
		return filter.getFeature(name);
	}

	public XMLReader getParent() {
		return filter.getParent();
	}

	public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
		return filter.getProperty(name);
	}

	public int hashCode() {
		return filter.hashCode();
	}

	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		filter.ignorableWhitespace(ch, start, length);
	}

	public void notationDecl(String name, String publicId, String systemId) throws SAXException {
		filter.notationDecl(name, publicId, systemId);
	}

	public void parse(InputSource input) throws SAXException, IOException {
		filter.parse(input);
	}

	public void parse(String systemId) throws SAXException, IOException {
		filter.parse(systemId);
	}

	public void processingInstruction(String target, String data) throws SAXException {
		filter.processingInstruction(target, data);
	}

	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		return filter.resolveEntity(publicId, systemId);
	}

	public void setContentHandler(ContentHandler handler) {
		filter.setContentHandler(handler);
	}

	public void setDTDHandler(DTDHandler handler) {
		filter.setDTDHandler(handler);
	}

	public void setDocumentLocator(Locator locator) {
		filter.setDocumentLocator(locator);
	}

	public void setEntityResolver(EntityResolver resolver) {
		filter.setEntityResolver(resolver);
	}

	public void setErrorHandler(ErrorHandler handler) {
		filter.setErrorHandler(handler);
	}

	public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
		filter.setFeature(name, value);
	}

	public void setParent(XMLReader parent) {
		filter.setParent(parent);
	}

	public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
		filter.setProperty(name, value);
	}

	public void skippedEntity(String name) throws SAXException {
		filter.skippedEntity(name);
	}

	public void startDocument() throws SAXException {
		filter.startDocument();
	}

	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		filter.startElement(uri, localName, qName, atts);
	}

	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		filter.startPrefixMapping(prefix, uri);
	}

	public String toString() {
		return filter.toString();
	}

	public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName)
			throws SAXException {
		filter.unparsedEntityDecl(name, publicId, systemId, notationName);
	}

	public void warning(SAXParseException e) throws SAXException {
		filter.warning(e);
	}

}
