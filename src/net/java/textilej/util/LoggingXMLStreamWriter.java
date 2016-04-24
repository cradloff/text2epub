package net.java.textilej.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class LoggingXMLStreamWriter implements XmlStreamWriter {

	private XmlStreamWriter delegate;
	private Logger logger = Logger.getLogger(LoggingXMLStreamWriter.class.getName());

	private List<String> elementNames = new ArrayList<String>();
	
	public LoggingXMLStreamWriter(XmlStreamWriter delegate) {
		this.delegate = delegate;
	}

	public void close() {
		delegate.close();
	}

	public void flush() {
		delegate.flush();
	}

	public String getPrefix(String uri) {
		return delegate.getPrefix(uri);
	}

	public void setDefaultNamespace(String uri) {
		delegate.setDefaultNamespace(uri);
	}

	public void setPrefix(String prefix, String uri) {
		delegate.setPrefix(prefix, uri);
	}

	public void writeAttribute(String prefix, String namespaceURI,
			String localName, String value) {
		delegate.writeAttribute(prefix, namespaceURI, localName, value);
	}

	public void writeAttribute(String namespaceURI, String localName,
			String value) {
		delegate.writeAttribute(namespaceURI, localName, value);
	}

	public void writeAttribute(String localName, String value) {
		delegate.writeAttribute(localName, value);
	}

	public void writeCData(String data) {
		delegate.writeCData(data);
	}

	public void writeCharacters(char[] text, int start, int len) {
		delegate.writeCharacters(text, start, len);
	}

	public void writeCharacters(String text) {
		delegate.writeCharacters(text);
	}

	public void writeLiteral(String literal) {
		delegate.writeLiteral(literal);
	}

	public void writeComment(String data) {
		delegate.writeComment(data);
	}

	public void writeDefaultNamespace(String namespaceURI) {
		delegate.writeDefaultNamespace(namespaceURI);
	}

	public void writeDTD(String dtd) {
		delegate.writeDTD(dtd);
	}

	public void writeEmptyElement(String prefix, String localName,
			String namespaceURI) {
		logger.info("Empty element["+elementNames.size()+"] "+localName);
		delegate.writeEmptyElement(prefix, localName, namespaceURI);
	}

	public void writeEmptyElement(String namespaceURI, String localName)
			{
		logger.info("Empty element["+elementNames.size()+"] "+localName);
		delegate.writeEmptyElement(namespaceURI, localName);
	}

	public void writeEmptyElement(String localName) {
		logger.info("Empty element["+elementNames.size()+"] "+localName);
		delegate.writeEmptyElement(localName);
	}

	public void writeEndDocument() {
		delegate.writeEndDocument();
	}

	public void writeEndElement() {
		if (elementNames.size() == 0) {
			throw new IllegalStateException("Too many end elements");
		}
		logger.info("End element["+elementNames.size()+"]: "+elementNames);
		elementNames.remove(elementNames.size()-1);
		delegate.writeEndElement();
	}

	public void writeEntityRef(String name) {
		delegate.writeEntityRef(name);
	}

	public void writeNamespace(String prefix, String namespaceURI)
			{
		delegate.writeNamespace(prefix, namespaceURI);
	}

	public void writeProcessingInstruction(String target, String data)
			{
		delegate.writeProcessingInstruction(target, data);
	}

	public void writeProcessingInstruction(String target)
			{
		delegate.writeProcessingInstruction(target);
	}

	public void writeStartDocument() {
		delegate.writeStartDocument();
	}

	public void writeStartDocument(String encoding, String version)
			{
		delegate.writeStartDocument(encoding, version);
	}

	public void writeStartDocument(String version) {
		delegate.writeStartDocument(version);
	}

	public void writeStartElement(String prefix, String localName,
			String namespaceURI) {
		elementNames.add(localName);
		logger.info("Start element["+elementNames.size()+"] "+elementNames);
		delegate.writeStartElement(prefix, localName, namespaceURI);
	}

	public void writeStartElement(String namespaceURI, String localName)
			{
		elementNames.add(localName);
		logger.info("Start element["+elementNames.size()+"] "+elementNames);
		delegate.writeStartElement(namespaceURI, localName);
	}

	public void writeStartElement(String localName) {
		elementNames.add(localName);
		logger.info("Start element["+elementNames.size()+"] "+elementNames);
		delegate.writeStartElement(localName);
	}
	
	
	
}
