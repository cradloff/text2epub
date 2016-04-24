package net.java.textilej.parser.markup.trac.token;

import net.java.textilej.parser.markup.PatternBasedElement;
import net.java.textilej.parser.markup.PatternBasedElementProcessor;

public class HyperlinkReplacementToken extends PatternBasedElement {

	@Override
	protected String getPattern(int groupOffset) {
		return "\\[\\s*([^\\]\\s]+)\\s*([^\\]]+)?\\s*\\]";
	}

	@Override
	protected int getPatternGroupCount() {
		return 2;
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new HyperlinkReplacementTokenProcessor();
	}
	
	private static class HyperlinkReplacementTokenProcessor extends PatternBasedElementProcessor {
		@Override
		public void emit() {
			String href = group(1);
			String text = group(2);
			if (text == null) {
				text = href;
			} else {
				text = text.trim();
				if (text.length() == 0) {
					text = href;
				}
			}
			
			builder.link(href, text);
		}
	}

}
