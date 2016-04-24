package net.java.textilej.parser;

import java.io.StringWriter;

import net.java.textilej.parser.builder.HtmlDocumentBuilder;
import net.java.textilej.parser.markup.Dialect;
import net.java.textilej.parser.markup.textile.TextileDialect;

/**
 * A parser that parses basic <a href="http://en.wikipedia.org/wiki/Textile_(markup_language)">Textile markup</a> and converts it to HTML.
 * 
 * Based on the spec available at <a href="http://textile.thresholdstate.com/">http://textile.thresholdstate.com/</a>,
 * supports basic phrase modifiers, block modifiers, attributes, footnotes, and some punctuation.
 * 
 * Additionally supported are <code>{toc}</code> and <code>{glossary}</code>.
 * 
 * The supported syntax of the parser may be augmented by {@link #setDialect(Dialect) setting a dialect}.  
 * Without adding a dialect the base syntax of the parser supports true Textile markup, with the addition of the following:
 * <ol>
 *   <li>Support for {toc} and {glossary}</li>
 *   <li>lists that start with '-'</li>
 *   <li>Confluence-style table headers</li>
 * </ol>
 * 
 * @author dgreen
 * 
 * @deprecated use {@link MarkupParser}
 */
@Deprecated
public class TextileParser {

	private DocumentBuilder builder;

	/**
	 * parse the given textile string and produce the result as an HTML document.
	 * 
	 * @param textile the textile to parse
	 * 
	 * @return the HTML document text.
	 */
	public String parseToHtml(String textile) {
		if (builder != null) {
			throw new IllegalStateException("parseToHtml cannot be called if builder is set");
		}
		StringWriter out = new StringWriter();
		setBuilder(new HtmlDocumentBuilder(out));
		parse(textile);
		setBuilder(null);
		return out.toString();
	}


	/**
	 * parse the given Textile markup string and emit the results as an HTML document to
	 * the given writer.  The given writer is closed upon return of this function.
	 * 
	 * @param textile the Textile markup
	 */
	public void parse(String textile) {
		parse(textile,true);
	}
	
	/**
	 * parse the given Textile markup string and emit the results as an HTML document to
	 * the given writer.  The given writer is closed upon return of this function.
	 * 
	 * if <code>asDocument</code> is specified, the {@link #getBuilder() builder} is treated as a document
	 * ({@link DocumentBuilder#beginDocument()} and {@link DocumentBuilder#endDocument()} are called).  
	 * 
	 * @param textile the Textile markup
	 * @param asDocument if true, the {@link #getBuilder() builder} is treated as a document
	 */
	public void parse(String textile,boolean asDocument) {
		if (builder == null) {
			throw new IllegalStateException("Must set builder");
		}
		
		MarkupParser parser = new MarkupParser();
		parser.setDialect(new TextileDialect());
		parser.setBuilder(builder);
		parser.parse(textile, asDocument);
	}	

	
	public void setBuilder(DocumentBuilder builder) {
		this.builder = builder;
	}

	public DocumentBuilder getBuilder() {
		return builder;
	}
	
}
