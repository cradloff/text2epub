package net.java.textilej.parser.markup.phrase;

import net.java.textilej.parser.markup.PatternBasedElementProcessor;



public class LiteralPhraseModifierProcessor extends PatternBasedElementProcessor {

	private final boolean escaping;
	
	public LiteralPhraseModifierProcessor(boolean escaping) {
		this.escaping = escaping;
	}
	
	@Override
	public void emit() {
		if (escaping) {
			getBuilder().characters(group(1));
		} else {
			getBuilder().charactersUnescaped(group(1));
		}
	}

}
