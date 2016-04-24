package net.java.textilej.parser.markup.confluence.phrase;

import java.util.regex.Pattern;

import net.java.textilej.parser.Attributes;
import net.java.textilej.parser.DocumentBuilder.SpanType;
import net.java.textilej.parser.markup.PatternBasedElementProcessor;
import net.java.textilej.parser.markup.PatternBasedElement;


public class SimpleWrappedPhraseModifier extends PatternBasedElement {

	protected static final int CONTENT_GROUP = 1;
	
	private static class SimplePhraseModifierProcessor extends PatternBasedElementProcessor {
		private final SpanType spanType;
		private final boolean nesting;
		
		public SimplePhraseModifierProcessor(SpanType spanType, boolean nesting) {
			this.spanType = spanType;
			this.nesting = nesting;
		}
		
		@Override
		public void emit() {
			Attributes attributes = new Attributes();
			getBuilder().beginSpan(spanType, attributes);
			if (nesting) {
				getDialect().emitMarkupLine(parser, state, state.getLineCharacterOffset() + getStart(this),
						getContent(this), 0);
			} else {
				getDialect().emitMarkupText(parser, state, getContent(this));
			}
			getBuilder().endSpan();
		}
	}
	
	private String startDelimiter;
	private String endDelimiter;
	private SpanType spanType;
	private final boolean nesting;
	
	public SimpleWrappedPhraseModifier(String startDelimiter,String endDelimiter, SpanType spanType, boolean nesting) {
		this.startDelimiter = startDelimiter;
		this.endDelimiter = endDelimiter;
		this.spanType = spanType;
		this.nesting = nesting;
	}

	@Override
	protected String getPattern(int groupOffset) {
		return 
		Pattern.quote(startDelimiter) +
		"([^\\s-](?:.*?[^\\s-])?)" + // content: note that we dont allow preceding '-' or trailing '-' to avoid conflict with strikethrough and emdash
		Pattern.quote(endDelimiter);
	}

	@Override
	protected int getPatternGroupCount() {
		return 1;
	}
	
	protected static String getContent(PatternBasedElementProcessor processor) {
		return processor.group(CONTENT_GROUP);
	}
	protected static int getStart(PatternBasedElementProcessor processor) {
		return processor.start(CONTENT_GROUP);
	}
	
	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new SimplePhraseModifierProcessor(spanType, nesting);
	}
}
