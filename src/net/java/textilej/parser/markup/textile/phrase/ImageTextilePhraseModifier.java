package net.java.textilej.parser.markup.textile.phrase;

import java.util.regex.Pattern;

import net.java.textilej.parser.ImageAttributes;
import net.java.textilej.parser.ImageAttributes.Align;
import net.java.textilej.parser.markup.PatternBasedElement;
import net.java.textilej.parser.markup.PatternBasedElementProcessor;
import net.java.textilej.parser.markup.textile.Textile;
import net.java.textilej.parser.markup.textile.TextileContentState;

public class ImageTextilePhraseModifier extends PatternBasedElement {

	protected static final int ALIGNMENT_GROUP = Textile.ATTRIBUTES_GROUP_COUNT+1;
	protected static final int CONTENT_GROUP = Textile.ATTRIBUTES_GROUP_COUNT+2;
	protected static final int ATTRIBUTES_OFFSET = 1;
	
	@Override
	protected String getPattern(int groupOffset) {
		String quotedDelimiter = Pattern.quote("!");
		
		return 
			quotedDelimiter +
			Textile.REGEX_ATTRIBUTES +
			"(<|>|=)?(\\S(?:.*?\\S)?)(\\([^\\)]+\\))?" + // content
			quotedDelimiter +
			"(:([^\\s]*[^\\s!.)(,]))?"; // optional hyperlink suffix
	}

	@Override
	protected int getPatternGroupCount() {
		return Textile.ATTRIBUTES_GROUP_COUNT+5;
	}
	
	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new ImagePhraseModifierProcessor();
	}
	
	private static class ImagePhraseModifierProcessor extends PatternBasedElementProcessor {
		@Override
		public void emit() {
			String alignment = group(ALIGNMENT_GROUP);
			String imageUrl = group(CONTENT_GROUP);
			String altAndTitle = group(CONTENT_GROUP+1);
			String href = group(CONTENT_GROUP+3);
			String namedLinkUrl = href==null?null:((TextileContentState)getState()).getNamedLinkUrl(href);
			if (namedLinkUrl != null) {
				href = namedLinkUrl;
			}
			
			ImageAttributes attributes = new ImageAttributes();
			attributes.setTitle(altAndTitle);
			attributes.setAlt(altAndTitle);
			if (alignment != null) {
				if ("<".equals(alignment)) {
					attributes.setAlign(Align.Left);
				} else if (">".equals(alignment)) {
					attributes.setAlign(Align.Right);
				} else if ("=".equals(alignment)) {
					attributes.setAlign(Align.Center);
				}
			}
			Textile.configureAttributes(this, attributes, ATTRIBUTES_OFFSET,false);
			if (href != null) {
				builder.imageLink(attributes, href, imageUrl);
			} else {
				builder.image(attributes, imageUrl);
			}
		}
	}

}
