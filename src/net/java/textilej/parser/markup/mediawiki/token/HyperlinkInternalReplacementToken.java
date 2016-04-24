package net.java.textilej.parser.markup.mediawiki.token;

import net.java.textilej.parser.Attributes;
import net.java.textilej.parser.LinkAttributes;
import net.java.textilej.parser.markup.PatternBasedElement;
import net.java.textilej.parser.markup.PatternBasedElementProcessor;
import net.java.textilej.parser.markup.mediawiki.MediaWikiDialect;

/**
 * match [[internal links]]
 * 
 * @author dgreen
 *
 */
public class HyperlinkInternalReplacementToken extends PatternBasedElement {

	@Override
	protected String getPattern(int groupOffset) {
		return "(?:\\[\\[([^\\]\\|]+?)\\s*(?:\\|\\s*([^\\]]*))?\\]\\])";
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
			String pageName = group(1);
			String altText = group(2);
			String href = ((MediaWikiDialect)getDialect()).toInternalHref(pageName);
			
			// category references start with ':' but are not referenced that way in the text
			if (pageName.startsWith(":")) {
				pageName = pageName.substring(1);
			}
			if (altText == null || altText.trim().length() == 0) {
				altText = pageName;
				if (altText.startsWith("#")) {
					altText = altText.substring(1);
				}
			}
			if (pageName.startsWith("#")) {
				builder.link(href, altText);
			} else {
				Attributes attributes = new LinkAttributes();
				attributes.setTitle(pageName);
				builder.link(attributes,href, altText);
			}
		}
	}

}
