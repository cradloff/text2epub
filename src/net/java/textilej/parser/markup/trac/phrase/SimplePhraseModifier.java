package net.java.textilej.parser.markup.trac.phrase;

import net.java.textilej.parser.DocumentBuilder.SpanType;

public class SimplePhraseModifier extends SimpleWrappedPhraseModifier {

	public SimplePhraseModifier(String delimiter, SpanType spanType) {
		super(delimiter,delimiter,new SpanType[] { spanType });
	}
	
	public SimplePhraseModifier(String delimiter, SpanType spanType,boolean nesting) {
		super(delimiter,delimiter,new SpanType[] { spanType },nesting);
	}

	public SimplePhraseModifier(String delimiter, SpanType[] spanType) {
		super(delimiter,delimiter,spanType);
	}
	
	public SimplePhraseModifier(String delimiter, SpanType[] spanType,boolean nesting) {
		super(delimiter,delimiter,spanType,nesting);
	}
}
