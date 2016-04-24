package net.java.textilej.parser.markup.confluence.token;

import net.java.textilej.parser.Attributes;
import net.java.textilej.parser.DocumentBuilder.SpanType;
import net.java.textilej.parser.markup.PatternBasedElement;
import net.java.textilej.parser.markup.PatternBasedElementProcessor;

public class AnchorReplacementToken extends PatternBasedElement {

	@Override
	protected String getPattern(int groupOffset) {
		return "\\{anchor:([^\\}]+)\\}";
	}

	@Override
	protected int getPatternGroupCount() {
		return 1;
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new AnchorReplacementTokenProcessor();
	}
	
	private static class AnchorReplacementTokenProcessor extends PatternBasedElementProcessor {
		@Override
		public void emit() {
			String name = group(1);
			Attributes attributes = new Attributes();
			attributes.setId(name);
			getBuilder().beginSpan(SpanType.SPAN, attributes);
			getBuilder().endSpan();
		}
		
	}

}
