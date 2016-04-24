package net.java.textilej.parser.markup.mediawiki.token;

import net.java.textilej.parser.markup.PatternBasedElement;
import net.java.textilej.parser.markup.PatternBasedElementProcessor;

/**
 * match [[internal links]]
 * 
 * @author dgreen
 *
 */
public class HyperlinkExternalReplacementToken extends PatternBasedElement {

	@Override
	protected String getPattern(int groupOffset) {
		return "(?:\\[([^\\[\\]\\|\\s]+)(?:(?:\\||\\s)([^\\]]*))?\\s*\\])";
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
			String altText = group(2);
			
			if (altText == null || altText.trim().length() == 0) {
				altText = href;
			}
			builder.link(href, altText);
		}
	}

}
