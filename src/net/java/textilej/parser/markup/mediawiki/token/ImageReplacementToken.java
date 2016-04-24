package net.java.textilej.parser.markup.mediawiki.token;

import net.java.textilej.parser.ImageAttributes;
import net.java.textilej.parser.ImageAttributes.Align;
import net.java.textilej.parser.markup.PatternBasedElement;
import net.java.textilej.parser.markup.PatternBasedElementProcessor;

/**
 * match [[Image:someImage.png]]
 * 
 * @author dgreen
 *
 */
public class ImageReplacementToken extends PatternBasedElement {

	@Override
	protected String getPattern(int groupOffset) {
		return "(?:\\[\\[Image:([^\\]\\|]+)(?:\\|([^\\]]*))?\\]\\])";
	}

	@Override
	protected int getPatternGroupCount() {
		return 2;
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new ImageReplacementTokenProcessor();
	}
	
	private static class ImageReplacementTokenProcessor extends PatternBasedElementProcessor {
		@Override
		public void emit() {
			String imageUrl = group(1);
			String optionsString = group(2);

			ImageAttributes attributes = new ImageAttributes();
			if (optionsString != null) {
				String[] options = optionsString.split("\\s*\\|\\s*");
				for (String option: options) {
					if ("center".equals(option)) {
						attributes.setAlign(Align.Middle);
					} else if ("left".equals(option)) {
						attributes.setAlign(Align.Left);
					} else if ("right".equals(option)) {
						attributes.setAlign(Align.Right);
					} else if ("none".equals(option)) {
						attributes.setAlign(null);
					} else if ("thumb".equals(option)||"thumbnail".equals(option)) {
						// ignore
					} else if (option.matches("\\d+px")) {
						try {
							int size = Integer.parseInt(option.substring(0,option.length()-2));
							attributes.setWidth(size);
							attributes.setHeight(size);
						} catch (NumberFormatException e) {
							// ignore
						}
					} else if ("frameless".equals(option)) {
						attributes.setBorder(0);
					} else if ("frame".equals(option)) {
						attributes.setBorder(1);
					} else {
						attributes.setTitle(option);
					}
				}
			}
			builder.image(attributes, imageUrl);
		}
	}

}
