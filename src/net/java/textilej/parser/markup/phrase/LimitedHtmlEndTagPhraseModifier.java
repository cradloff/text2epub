package net.java.textilej.parser.markup.phrase;

import net.java.textilej.parser.markup.PatternBasedElement;
import net.java.textilej.parser.markup.PatternBasedElementProcessor;


public class LimitedHtmlEndTagPhraseModifier extends PatternBasedElement {

	private String pattern;
	
	public LimitedHtmlEndTagPhraseModifier(String... elementNames) {
		StringBuilder buf = new StringBuilder();
		buf.append("(</");
		buf.append("(?:");
		int index = 0;
		for (String elementName: elementNames) {
			if (index++ > 0) {
				buf.append("|");
			}
			buf.append(elementName);
		}
		buf.append(")\\s*>)");
		pattern = buf.toString();
	}
	
	@Override
	protected String getPattern(int groupOffset) {
		return pattern;
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new LiteralPhraseModifierProcessor(false);
	}
	
	@Override
	protected int getPatternGroupCount() {
		return 1;
	}

}
