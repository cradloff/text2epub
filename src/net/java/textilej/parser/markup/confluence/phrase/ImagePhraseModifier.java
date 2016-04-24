package net.java.textilej.parser.markup.confluence.phrase;

import net.java.textilej.parser.Attributes;
import net.java.textilej.parser.markup.PatternBasedElement;
import net.java.textilej.parser.markup.PatternBasedElementProcessor;



public class ImagePhraseModifier extends PatternBasedElement {

	protected static final int CONTENT_GROUP = 1;
	
	@Override
	protected String getPattern(int groupOffset) {
		
		return  "!([^\\|!\\s]+)(?:\\|([^!]*))?!";
	}

	@Override
	protected int getPatternGroupCount() {
		return 2;
	}
	
	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new ImagePhraseModifierProcessor();
	}
	
	private static class ImagePhraseModifierProcessor extends PatternBasedElementProcessor {
		@Override
		public void emit() {
			String imageUrl = group(CONTENT_GROUP);

			Attributes attributes = new Attributes();
			builder.image(attributes, imageUrl);
		}
	}

}
