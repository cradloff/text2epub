package net.java.textilej.parser.markup.phrase;

import net.java.textilej.parser.markup.PatternBasedElement;
import net.java.textilej.parser.markup.PatternBasedElementProcessor;




public class HtmlEndTagPhraseModifier extends PatternBasedElement {

	@Override
	protected String getPattern(int groupOffset) {
		return "(</[a-zA-Z][a-zA-Z0-9_:-]*\\s*>)";
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new LiteralPhraseModifierProcessor(false);
	}

	@Override
	protected int getPatternGroupCount() {
		return 1;
	}

}
