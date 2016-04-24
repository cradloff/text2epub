package net.java.textilej.parser.markup;

import net.java.textilej.parser.DocumentBuilder;
import net.java.textilej.parser.MarkupParser;

public class Processor {
	protected Dialect dialect;
	protected DocumentBuilder builder;
	protected MarkupParser parser;
	protected ContentState state;

	public Processor() {
		super();
	}

	/**
	 * The builder that is the target for output
	 */
	public DocumentBuilder getBuilder() {
		return builder;
	}

	/**
	 * The parser that is actively using this processor
	 */
	public MarkupParser getParser() {
		return parser;
	}

	public void setParser(MarkupParser parser) {
		this.parser = parser;
		this.builder = (parser==null)?null:parser.getBuilder();
		dialect = (parser == null)?null:parser.getDialect();
	}

	public Dialect getDialect() {
		return dialect;
	}

	public ContentState getState() {
		return state;
	}

	public void setState(ContentState state) {
		this.state = state;
	}
	
}