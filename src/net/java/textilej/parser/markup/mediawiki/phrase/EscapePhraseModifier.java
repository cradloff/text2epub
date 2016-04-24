package net.java.textilej.parser.markup.mediawiki.phrase;



import net.java.textilej.parser.markup.PatternBasedElement;
import net.java.textilej.parser.markup.PatternBasedElementProcessor;
import net.java.textilej.parser.markup.phrase.LiteralPhraseModifierProcessor;

public class EscapePhraseModifier extends PatternBasedElement {

	@Override
	protected String getPattern(int groupOffset) {
		return 
		"<nowiki>" +
		"(\\S(?:.*?\\S)?)" + // content
		"</nowiki>";
	}

	@Override
	protected int getPatternGroupCount() {
		return 1;
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new LiteralPhraseModifierProcessor(true);
	}

}
