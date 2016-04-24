package net.java.textilej.parser.util;

import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import net.java.textilej.parser.markup.Dialect;
import net.java.textilej.parser.outline.OutlineItem;
import net.java.textilej.parser.outline.OutlineParser;
import net.java.textilej.util.DefaultXmlStreamWriter;
import net.java.textilej.util.FormattingXMLStreamWriter;
import net.java.textilej.util.XmlStreamWriter;

public class MarkupToEclipseToc {
	private Dialect dialect;
	private String bookTitle;
	private String htmlFile;
	
	public String parse(String markupContent) {
		if (dialect == null) {
			throw new IllegalStateException();
		}
		OutlineParser parser = new OutlineParser(dialect);
		
		OutlineItem root = parser.parse(markupContent);
		
		StringWriter out = new StringWriter(8096);
		
		XmlStreamWriter writer = createXmlStreamWriter(out);
		
		writer.writeStartDocument("utf-8","1.0");
		
		writer.writeStartElement("toc");
		writer.writeAttribute("topic", getHtmlFile());
		writer.writeAttribute("label", getBookTitle());
		
		emitToc(writer,root.getChildren());
		
		writer.writeEndElement(); // toc
		
		writer.writeEndDocument();
		writer.close();
		
		return out.toString();
		
	}

	private void emitToc(XmlStreamWriter writer, List<OutlineItem> children) {
		for (OutlineItem item: children) {
			writer.writeStartElement("topic");
			writer.writeAttribute("href", getHtmlFile()+"#"+item.getId());
			writer.writeAttribute("label", item.getLabel());
			
			if (!item.getChildren().isEmpty()) {
				emitToc(writer,item.getChildren());
			}
			
			writer.writeEndElement(); // topic
		}
	}
	

	public String getBookTitle() {
		return bookTitle;
	}

	public void setBookTitle(String bookTitle) {
		this.bookTitle = bookTitle;
	}


	public String getHtmlFile() {
		return htmlFile;
	}

	public void setHtmlFile(String htmlFile) {
		this.htmlFile = htmlFile;
	}

	protected XmlStreamWriter createXmlStreamWriter(Writer out) {
		XmlStreamWriter writer = new DefaultXmlStreamWriter(out);
		return new FormattingXMLStreamWriter(writer);
	}

	public Dialect getDialect() {
		return dialect;
	}

	public void setDialect(Dialect dialect) {
		this.dialect = dialect;
	}

}
