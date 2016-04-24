package net.java.textilej.parser.markup.textile.token;

import net.java.textilej.parser.Attributes;
import net.java.textilej.parser.DocumentBuilder.SpanType;
import net.java.textilej.parser.markup.PatternBasedElement;
import net.java.textilej.parser.markup.PatternBasedElementProcessor;

public class FootnoteReferenceReplacementToken extends PatternBasedElement {

	@Override
	protected String getPattern(int groupOffset) {
		return "(?:\\[(\\d+)\\])";
	}

	@Override
	protected int getPatternGroupCount() {
		return 1;
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new FootnoteReferenceReplacementTokenProcessor();
	}
	
	private static class FootnoteReferenceReplacementTokenProcessor extends PatternBasedElementProcessor {
		@Override
		public void emit() {
			String footnote = group(1);
			String htmlId = state.getFootnoteId(footnote);
			builder.beginSpan(SpanType.SUPERSCRIPT, new Attributes(null,"footnote",null,null));
			builder.link("#"+htmlId, footnote);
			builder.endSpan();
		}
	}

}
