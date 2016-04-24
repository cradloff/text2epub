package net.java.textilej.parser.markup.textile.phrase;

import java.util.regex.Pattern;

import net.java.textilej.parser.markup.PatternBasedElement;
import net.java.textilej.parser.markup.PatternBasedElementProcessor;
import net.java.textilej.parser.markup.phrase.LiteralPhraseModifierProcessor;

public class EscapeTextilePhraseModifier extends PatternBasedElement {

	@Override
	protected String getPattern(int groupOffset) {
		String quotedDelimiter = Pattern.quote("==");
		
		return 
		quotedDelimiter +
		"(\\S(?:.*?\\S)?)" + // content
		quotedDelimiter;
	}

	@Override
	protected int getPatternGroupCount() {
		return 1;
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new LiteralPhraseModifierProcessor(false);
	}

}
