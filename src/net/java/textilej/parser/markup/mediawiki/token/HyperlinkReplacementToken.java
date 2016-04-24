package net.java.textilej.parser.markup.mediawiki.token;

import net.java.textilej.parser.markup.PatternBasedElement;
import net.java.textilej.parser.markup.PatternBasedElementProcessor;

public class HyperlinkReplacementToken extends PatternBasedElement {

	@Override
	protected String getPattern(int groupOffset) {
		return "(?:(\"|\\!)([^\"]+)\\"+(1+groupOffset)+":([^\\s]+))";
	}

	@Override
	protected int getPatternGroupCount() {
		return 3;
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new HyperlinkReplacementTokenProcessor();
	}
	
	private static class HyperlinkReplacementTokenProcessor extends PatternBasedElementProcessor {
		@Override
		public void emit() {
			String hyperlinkBoundaryText = group(1);
			String hyperlinkSrc = group(2);
			String href = group(3);
			
			if (hyperlinkBoundaryText.equals("\"")) {
				builder.link(href, hyperlinkSrc);
			} else {
				builder.imageLink(href, hyperlinkSrc);
			}
		}
	}

}
