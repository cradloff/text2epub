package net.java.textilej.util;

public interface XmlStreamWriter {

	public void close();

	public void flush();

	public String getPrefix(String uri);

	public void setDefaultNamespace(String uri);

	public void setPrefix(String prefix, String uri);

	public void writeAttribute(String localName, String value);

	public void writeAttribute(String namespaceURI, String localName,
			String value);

	public void writeAttribute(String prefix, String namespaceURI,
			String localName, String value);

	public void writeCData(String data);

	public void writeCharacters(String text);

	public void writeCharacters(char[] text, int start, int len);

	public void writeComment(String data);

	public void writeDTD(String dtd);

	public void writeDefaultNamespace(String namespaceURI);

	public void writeEmptyElement(String localName);

	public void writeEmptyElement(String namespaceURI, String localName);

	public void writeEmptyElement(String prefix, String localName,
			String namespaceURI);

	public void writeEndDocument();

	public void writeEndElement();

	public void writeEntityRef(String name);

	public void writeNamespace(String prefix, String namespaceURI);

	public void writeProcessingInstruction(String target);

	public void writeProcessingInstruction(String target, String data);

	public void writeStartDocument();

	public void writeStartDocument(String version);

	public void writeStartDocument(String encoding, String version);

	public void writeStartElement(String localName);

	public void writeStartElement(String namespaceURI, String localName);

	public void writeStartElement(String prefix, String localName,
			String namespaceURI);

	/**
	 * Write an XML fragment directly to the output.  The given text is not processed or XML-encoded,
	 * since it is assumed to be a legal XML fragment.
	 */
	public void writeLiteral(String literal);

}