package net.java.textilej.parser.markup.trac.token;

import net.java.textilej.parser.markup.PatternBasedElement;
import net.java.textilej.parser.markup.PatternBasedElementProcessor;
import net.java.textilej.parser.markup.phrase.LiteralPhraseModifierProcessor;

/**
 * A token replacement that replaces any two characters that start with a '!' and are followed by
 * a trac markup character
 * 
 * @author dgreen
 *
 */
public class BangEscapeToken extends PatternBasedElement {

	@Override
	protected String getPattern(int groupOffset) {
		return "!(['\\}\\{\\^,_\\-])";
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
