package net.java.textilej.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import net.java.textilej.parser.builder.HtmlDocumentBuilder;
import net.java.textilej.parser.markup.Dialect;

/**
 * A markup processor that can process text markup formats such as Textile.
 * 
 * @author dgreen
 *
 */
public class MarkupParser {

	private Dialect dialect;
	private DocumentBuilder builder;
	
	public MarkupParser() {}
	
	public MarkupParser(Dialect dialect, DocumentBuilder builder) {
		this.dialect = dialect;
		this.builder = builder;
	}
	
	public MarkupParser(Dialect dialect) {
		this.dialect = dialect;
	}

	/**
	 * the dialect of the markup to process
	 */
	public Dialect getDialect() {
		return dialect;
	}

	/**
	 * set the dialect of the markup to process
	 */
	public void setDialect(Dialect dialect) {
		this.dialect = dialect;
	}

	/**
	 * the builder to which parse results are propagated
	 */
	public DocumentBuilder getBuilder() {
		return builder;
	}

	/**
	 * set the builder to which parse results are propagated
	 */
	public void setBuilder(DocumentBuilder builder) {
		this.builder = builder;
	}

	public void parse(Reader markupContent) throws IOException {
		parse(markupContent,true);
	}
	
	public void parse(Reader markupContent,boolean asDocument) throws IOException {
		parse(readFully(markupContent),asDocument);
	}

	public void parse(String markupContent) {
		parse(markupContent,true);
	}
	
	public void parse(String markupContent,boolean asDocument) {
		if (dialect == null) {
			throw new IllegalStateException("markup dialect is not set");
		}
		if (builder == null) {
			throw new IllegalStateException("builder is not set");
		}
		dialect.processContent(this,markupContent,asDocument);
	}
	
	private String readFully(Reader reader) throws IOException {
		StringWriter writer = new StringWriter();
		int c;
		while ((c = reader.read()) != -1) {
			writer.write(c);
		}
		return writer.toString();
	}
	

	/**
	 * parse the given markup content and produce the result as an HTML document.
	 * 
	 * @param markupContent the textile to parse
	 * 
	 * @return the HTML document text.
	 */
	public String parseToHtml(String markupContent) {
		if (builder != null) {
			throw new IllegalStateException("Builder must not be set");
		}
		
		StringWriter out = new StringWriter();

		setBuilder(new HtmlDocumentBuilder(out));

		parse(markupContent);
		
		setBuilder(null);
		
		return out.toString();
	}
}
