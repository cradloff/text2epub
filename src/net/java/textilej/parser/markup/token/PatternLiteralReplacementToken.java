package net.java.textilej.parser.markup.token;

import net.java.textilej.parser.markup.PatternBasedElement;
import net.java.textilej.parser.markup.PatternBasedElementProcessor;

public class PatternLiteralReplacementToken extends PatternBasedElement {

	private String pattern;
	private String replacement;
	
	public PatternLiteralReplacementToken(String pattern,String replacement) {
		this.pattern = pattern;
		this.replacement = replacement;
	}

	@Override
	protected String getPattern(int groupOffset) {
		return pattern;
	}

	@Override
	protected int getPatternGroupCount() {
		return 1;
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new LiteralReplacementTokenProcessor(replacement);
	}

}
