package net.java.textilej.parser.markup.confluence.token;

import net.java.textilej.parser.Attributes;
import net.java.textilej.parser.LinkAttributes;
import net.java.textilej.parser.markup.PatternBasedElement;
import net.java.textilej.parser.markup.PatternBasedElementProcessor;

public class HyperlinkReplacementToken extends PatternBasedElement {
	
	@Override
	protected String getPattern(int groupOffset) {
		return "\\[([^\\]]+)\\]";
	}

	@Override
	protected int getPatternGroupCount() {
		return 1;
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new HyperlinkReplacementTokenProcessor();
	}

	private static class HyperlinkReplacementTokenProcessor extends PatternBasedElementProcessor {
		@Override
		public void emit() {
			String linkComposite = group(1);
			String[] parts = linkComposite.split("\\s*\\|\\s*");
			String text = parts.length > 1?parts[0]:null;
			String href = parts.length > 1?parts[1]:parts[0];
			String tip = parts.length > 2?parts[2]:null;
			if (text == null || text.length() == 0) {
				text = href;
			}
			Attributes attributes = new LinkAttributes();
			attributes.setTitle(tip);
			getBuilder().link(attributes,href, text);
		}
	}
}
