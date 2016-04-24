package net.java.textilej.parser.util;

import java.io.StringWriter;
import java.io.Writer;

import net.java.textilej.parser.MarkupParser;
import net.java.textilej.parser.builder.DocBookDocumentBuilder;
import net.java.textilej.parser.markup.Dialect;
import net.java.textilej.util.XmlStreamWriter;

public class MarkupToDocbook {
	private Dialect dialect;
	
	private String bookTitle;
	
	public String parse(String markupContent) throws Exception {
		if (dialect == null) {
			throw new IllegalStateException();
		}
		
		StringWriter out = new StringWriter();
		
		
		DocBookDocumentBuilder builder = new DocBookDocumentBuilder(out) {
			@Override
			protected XmlStreamWriter createXmlStreamWriter(Writer out) {
				return super.createFormattingXmlStreamWriter(out);
			}
		};
		builder.setBookTitle(bookTitle);

		MarkupParser textileParser = new MarkupParser();
		
		textileParser.setBuilder(builder);
		textileParser.setDialect(dialect);
		
		textileParser.parse(markupContent);
		
		return out.toString();
	}
	

	public String getBookTitle() {
		return bookTitle;
	}

	public void setBookTitle(String bookTitle) {
		this.bookTitle = bookTitle;
	}


	public Dialect getDialect() {
		return dialect;
	}


	public void setDialect(Dialect dialect) {
		this.dialect = dialect;
	}

}
